package roflanbuldyga.cheapsms.api

import roflanbuldyga.cheapsms.api.data.request.ActivationStatus
import roflanbuldyga.cheapsms.api.data.response.GetStatusResult
import roflanbuldyga.cheapsms.api.data.response.OrderNumberResult
import roflanbuldyga.cheapsms.api.data.response.SetStatusResult
import java.math.BigDecimal

interface CheapSMSApi {
    suspend fun getServices(apiKey: String): Map<String, Int>

    suspend fun getBalance(apiKey: String): BigDecimal

    suspend fun orderNumber(apiKey: String, service: String): OrderNumberResult

    suspend fun retryNumber(apiKey: String, operationId: Long): OrderNumberResult

    suspend fun setStatus(apiKey: String, operationId: Long, status: ActivationStatus): SetStatusResult

    suspend fun getStatus(apiKey: String): GetStatusResult
}