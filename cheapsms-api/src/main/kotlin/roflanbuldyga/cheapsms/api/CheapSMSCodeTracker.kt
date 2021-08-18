package roflanbuldyga.cheapsms.api

interface CheapSMSCodeTracker {
    fun trackOrder(operationId: Long, isRetryAttempt: Boolean)

    fun stopTrackingOrder(operationId: Long)

    interface Listener {
        fun onCodeReceived(operationId: Long, code: String, isRetryCode: Boolean)
    }
}

