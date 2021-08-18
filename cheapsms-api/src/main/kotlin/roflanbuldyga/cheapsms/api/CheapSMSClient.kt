package roflanbuldyga.cheapsms.api

import roflanbuldyga.cheapsms.api.data.request.ActivationStatus
import roflanbuldyga.cheapsms.api.data.response.GetStatusResult
import roflanbuldyga.cheapsms.api.data.response.OrderNumberResult
import roflanbuldyga.cheapsms.api.data.response.SetStatusResult
import roflanbuldyga.cheapsms.api.http.ApiCall
import roflanbuldyga.cheapsms.api.http.ApiHttpClient
import roflanbuldyga.cheapsms.api.http.ResponseParser
import roflanbuldyga.cheapsms.api.parser.*
import java.math.BigDecimal

class CheapSMSClient(config: CheapSMSConfig = config(true)) : CheapSMSApi {
    companion object {
        const val GET_SERVICES_ACTION = "getNumbersStatus"
        const val GET_BALANCE_ACTION = "getBalance"
        const val ORDER_NUMBER_ACTION = "getNumber"
        const val RETRY_NUMBER_ACTION = "retryNumber"
        const val SET_STATUS_ACTION = "setStatus"
        const val GET_STATUS_ACTION = "getStatus"

        private val getServicesResponseParser = GetServicesResponseParser()
        private val getBalanceResponseParser = GetBalanceResponseParser()
        private val orderNumberResponseParser = OrderNumberResponseParser()
        private val setStatusResponseParser = SetStatusResponseParser()
        private val getStatusResponseParser = GetStatusResponseParser()
    }

    internal val apiHttpClient = ApiHttpClient(config.api1Url, config.debugMode)


    override suspend fun getServices(
        apiKey: String
    ): Map<String, Int> = apiCall(
        getServicesResponseParser, apiKey, GET_SERVICES_ACTION
    )

    override suspend fun getBalance(
        apiKey: String
    ): BigDecimal = apiCall(
        getBalanceResponseParser, apiKey, GET_BALANCE_ACTION
    )

    override suspend fun orderNumber(
        apiKey: String,
        service: String
    ): OrderNumberResult = apiCall(
        orderNumberResponseParser, apiKey, ORDER_NUMBER_ACTION
    ) {
        put("service", service)
    }

    override suspend fun retryNumber(
        apiKey: String,
        operationId: Long
    ): OrderNumberResult = apiCall(
        orderNumberResponseParser, apiKey, RETRY_NUMBER_ACTION
    ) {
        put("id", operationId.toString())
    }

    override suspend fun setStatus(
        apiKey: String,
        operationId: Long,
        status: ActivationStatus
    ): SetStatusResult = apiCall(
        setStatusResponseParser, apiKey, SET_STATUS_ACTION
    ) {
        put("id", operationId.toString())
        put("status", status.code.toString())
    }

    override suspend fun getStatus(
        apiKey: String
    ): GetStatusResult = apiCall(
        getStatusResponseParser, apiKey, GET_STATUS_ACTION
    )


    private suspend inline fun <Response> apiCall(
        responseParser: ResponseParser<Response>,
        apiKey: String,
        action: String,
        paramsBuilder: MutableMap<String, String>.() -> Unit = { }
    ) = apiHttpClient.executeApiCall(
        ApiCall.create(
            responseParser = responseParser,
            apiKey = apiKey,
            action = action,
            params = HashMap<String, String>().apply(paramsBuilder)
        )
    )
}