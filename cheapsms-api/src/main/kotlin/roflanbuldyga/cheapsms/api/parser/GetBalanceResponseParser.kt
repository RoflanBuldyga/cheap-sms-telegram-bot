package roflanbuldyga.cheapsms.api.parser

import roflanbuldyga.cheapsms.api.http.ResponseParser
import java.math.BigDecimal

class GetBalanceResponseParser : ResponseParser<BigDecimal>() {
    override fun parseData(response: String): BigDecimal {
        val parts = response.split(":")
        return BigDecimal(parts[1])
    }
}