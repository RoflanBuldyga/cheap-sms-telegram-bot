package roflanbuldyga.cheapsms.api

import kotlinx.coroutines.*
import roflanbuldyga.cheapsms.api.data.response.GetStatusResult
import roflanbuldyga.cheapsms.api.data.response.OperationStatus
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

interface CheapSMSCodeTracker {
    fun trackOrder(apiKey: String, operationId: Long, isRetryAttempt: Boolean)

    fun stopTrackingOrder(operationId: Long)

    interface Listener {
        fun onCodeReceived(operationId: Long, isRetryCode: Boolean, code: String)

        fun onTrackingTimeout(operationId: Long, isRetryAttempt: Boolean)
    }
}

data class TrackableOrder(
    val apiKey: String,
    val operationId: Long,
    val isRetryAttempt: Boolean
) : Comparable<TrackableOrder> {
    val expiresAt: Long = System.currentTimeMillis() + CheapSMSCodeTrackerImpl.TRACKING_TIMEOUT

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
    }

    var isActive = AtomicBoolean(false)

    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("codeTracker")
    private val threadPool = (Executors.newScheduledThreadPool(1) as ScheduledThreadPoolExecutor).apply {
        removeOnCancelPolicy = true
    }
    private val ordersQueue = PriorityBlockingQueue<TrackableOrder>()
    private var scheduledFuture = AtomicReference<ScheduledFuture<*>?>(null)

    override fun trackOrder(apiKey: String, operationId: Long, isRetryAttempt: Boolean) {
        ordersQueue.add(TrackableOrder(apiKey, operationId, isRetryAttempt))

        if (ordersQueue.size == 1) {
            startTracking()
        }
    }

    override fun stopTrackingOrder(operationId: Long) {
        TODO()
    }

    private fun startTracking() {
        isActive.set(true)

        scheduledFuture.set(
            threadPool.scheduleAtFixedRate(::pullOrderStatus, 0, TASK_INTERVAL, TimeUnit.SECONDS)
        )
    }

    private fun stopTracking() {
        isActive.set(false)

//        threadPool.remove(::pullOrdersStatus)
        scheduledFuture.get()?.cancel(false)
    }

    private fun checkSize() {
        if (ordersQueue.size == 0) {
            stopTracking()
        }
    }

    private fun pullOrderStatus() {
        checkSize()

        val order = ordersQueue.take()
        val currentTime = System.currentTimeMillis()

        if (currentTime > order.expiresAt) {
            // fire Listener.onTrackingTimeout
            listener.onTrackingTimeout(order.operationId, order.isRetryAttempt)
        } else if (currentTime - order.lastRequestTimestamp > TRACKING_INTERVAL) {
            // send track request

            // if received a code then fire Listener.onCodeReceived
            // else add the order back to the queue

            scope.launch {
                val operationStatus = apiHttpClient.getStatus(order.apiKey, order.operationId)
                if (operationStatus.status == OperationStatus.STATUS_OK) {
                    listener.onCodeReceived(order.operationId, order.isRetryAttempt, operationStatus.code!!)
                } else {
                    ordersQueue.add(order.update(currentTime))
                }
            }
        } else {
            ordersQueue.add(order)
        }
    }
}

