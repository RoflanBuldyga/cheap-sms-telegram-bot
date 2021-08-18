package roflanbuldyga.cheapsms.api.parser

import roflanbuldyga.cheapsms.api.data.response.OrderNumberFailureReason
import roflanbuldyga.cheapsms.api.data.response.OrderNumberResult
import roflanbuldyga.cheapsms.api.http.ResponseParser

class OrderNumberResponseParser : ResponseParser<OrderNumberResult>() {
    override fun parseData(response: String): OrderNumberResult {
        val isOk = response.startsWith("ACCESS_NUMBER")
        var operationId: Long? = null
        var number: String? = null
        var failureReason: OrderNumberFailureReason? = null

        if (isOk) {
            parseParts(response) { parts ->
                operationId = parts[1].toLong()
                number = parts[2]
            }
        } else {
            failureReason = OrderNumberFailureReason.values().firstOrNull { response.startsWith(it.name) }!!
        }

        return OrderNumberResult(isOk, operationId, number, failureReason)
    }
}