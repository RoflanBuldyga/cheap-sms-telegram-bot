package roflanbuldyga.cheapsms.api

import kotlinx.coroutines.*
import roflanbuldyga.cheapsms.api.data.response.ErrorResponse
import roflanbuldyga.cheapsms.api.data.response.OperationStatus
import roflanbuldyga.cheapsms.api.exception.CheapSMSApiException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

interface CheapSMSCodeTracker {
    fun addOrder(apiKey: String, operationId: Long, isRetryAttempt: Boolean)

    fun removeOrder(operationId: Long)

    interface Listener {
        fun onCodeReceived(operationId: Long, isRetryAttempt: Boolean, code: String)

        fun onErrorOccurred(operationId: Long, isRetryAttempt: Boolean, error: ErrorResponse)

        fun onTrackingTimeout(operationId: Long, isRetryAttempt: Boolean)
    }
}

data class TrackableOrder internal constructor(
    val apiKey: String,
    val operationId: Long,
    val isRetryAttempt: Boolean,
    val expiresAt: Long
) : Comparable<TrackableOrder> {
    var requestCounter: Int = 0
    var lastRequestTimestamp: Long = -1L

    fun update(timestamp: Long) = apply {
        requestCounter++
        lastRequestTimestamp = timestamp
    }

    override fun compareTo(other: TrackableOrder): Int {
        return lastRequestTimestamp.compareTo(other.lastRequestTimestamp)
    }
}

class CheapSMSCodeTrackerImpl(
    val listener: CheapSMSCodeTracker.Listener,
    val apiHttpClient: CheapSMSClient
) : CheapSMSCodeTracker {
    companion object {
        // 1 seconds
        const val TASK_INTERVAL = 1000L

        // 3 seconds
        const val TRACKING_INTERVAL = 3000L

        // 5 minutes
        const val TRACKING_TIMEOUT = 5 * 60 * 1000L

        fun createTrackableOrder(
            apiKey: String,
            operationId: Long,
            isRetryAttempt: Boolean
        ) = TrackableOrder(
            apiKey, operationId, isRetryAttempt,
            expiresAt = System.currentTimeMillis() + TRACKING_TIMEOUT
        )
    }

    var isActive = AtomicBoolean(false)

    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("codeTracker")
    private val threadPool = (Executors.newScheduledThreadPool(1) as ScheduledThreadPoolExecutor).apply {
        removeOnCancelPolicy = true
    }
    private val orderQueue = PriorityBlockingQueue<TrackableOrder>()
    private var scheduledFuture = AtomicReference<ScheduledFuture<*>?>(null)

    override fun addOrder(apiKey: String, operationId: Long, isRetryAttempt: Boolean) {
        orderQueue.put(createTrackableOrder(apiKey, operationId, isRetryAttempt))

        if (!isActive.get()) {
            isActive.set(true)
            startTracking()
        }
    }

    override fun removeOrder(operationId: Long) {
        orderQueue.removeIf { it.operationId == operationId }
    }

    private fun startTracking() {
        isActive.set(true)

        scheduledFuture.set(
            threadPool.scheduleAtFixedRate(::track, 0, TASK_INTERVAL, TimeUnit.SECONDS)
        )
    }

    private fun stopTracking() {
        isActive.set(false)

        scheduledFuture.getAndSet(null)?.cancel(false)
    }

    private fun checkSize(): Boolean = (orderQueue.isEmpty()).also { isEmpty ->
        if (isEmpty) stopTracking()
    }

    private fun track() {
        val currentTime = System.currentTimeMillis()

        if (checkSize()) return

        val order = orderQueue.take()

        if (currentTime >= order.expiresAt) {
            // fire Listener.onTrackingTimeout
            listener.onTrackingTimeout(order.operationId, order.isRetryAttempt)
        } else if (currentTime - order.lastRequestTimestamp >= TRACKING_INTERVAL) {
            // send track request

            // if received a code then fire Listener.onCodeReceived
            // else add the order back to the queue

            scope.launch {
                try {
                    val operationStatus = apiHttpClient.getStatus(order.apiKey, order.operationId)
                    if (operationStatus.status == OperationStatus.STATUS_OK) {
                        listener.onCodeReceived(order.operationId, order.isRetryAttempt, operationStatus.code!!)
                    } else {
                        orderQueue.add(order.update(currentTime))
                    }
                } catch (ex: CheapSMSApiException) {
                    listener.onErrorOccurred(order.operationId, order.isRetryAttempt, ex.error)
                }
            }
        } else {
            // skip the order
            orderQueue.add(order)
        }
    }
}