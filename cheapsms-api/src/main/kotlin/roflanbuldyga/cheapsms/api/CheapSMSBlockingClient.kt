package roflanbuldyga.cheapsms.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import roflanbuldyga.cheapsms.api.data.request.ActivationStatus

// залупная хуйня потому что ни либа телеграм бота, ни айдар не умеют в корутины (я тоже ыаываыа)
class CheapSMSBlockingClient(config: CheapSMSConfig = config(true)) {
    val asyncClient = CheapSMSClient(config)

    fun getServices(apiKey: String) = syncCall { getServices(apiKey) }

    fun getBalance(apiKey: String) = syncCall { getBalance(apiKey) }

    fun orderNumber(apiKey: String, service: String) = syncCall { orderNumber(apiKey, service) }

    fun retryNumber(apiKey: String, operationId: Long) = syncCall { retryNumber(apiKey, operationId) }

    fun setStatus(apiKey: String, operationId: Long, status: ActivationStatus) =
        syncCall { setStatus(apiKey, operationId, status) }

    fun getStatus(apiKey: String, operationId: Long) = syncCall { getStatus(apiKey, operationId) }


    private fun <R> syncCall(call: suspend CheapSMSApi.() -> R) = runBlocking(Dispatchers.Default) {
        call.invoke(asyncClient)
    }
}