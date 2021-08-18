package roflanbuldyga.cheapsms.api.parser

import roflanbuldyga.cheapsms.api.data.response.SetStatusResult
import roflanbuldyga.cheapsms.api.http.ResponseParser

class SetStatusResponseParser : ResponseParser<SetStatusResult>() {
    override fun parseData(response: String): SetStatusResult {
        return parseEnum<SetStatusResult>(response)!!
    }
}

