package roflanbuldyga.cheapsms.api.parser

import roflanbuldyga.cheapsms.api.data.response.GetStatusResult
import roflanbuldyga.cheapsms.api.data.response.OperationStatus
import roflanbuldyga.cheapsms.api.http.ResponseParser

class GetStatusResponseParser : ResponseParser<GetStatusResult>() {
    override fun parseData(response: String): GetStatusResult {
        val status = parseEnum<OperationStatus>(response)!!
        var code: String? = null
        var lastCode: String? = null

        if (status == OperationStatus.STATUS_OK) {
            parseParts(response) { parts -> code = parts[1] }
        } else if (status == OperationStatus.STATUS_WAIT_RETRY) {
            parseParts(response) { parts -> lastCode = parts[1] }
        }

        return GetStatusResult(status, code, lastCode)
    }
}