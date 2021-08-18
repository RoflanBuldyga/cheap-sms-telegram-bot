package roflanbuldyga.cheapsms.api.parser

import roflanbuldyga.cheapsms.api.http.ResponseParser

class GetServicesResponseParser : ResponseParser<Map<String, Int>>() {
    override fun parseData(response: String): Map<String, Int> {
        require(response.first() == '{' && response.last() == '}') { "Malformed JSON:\n$response" }

        return response.substring(1, response.lastIndex).split(',')
            .associate { entry ->
                entry.split(':')
                    .map { it.trim().trimQuotes() }
                    .let { it[0] to it[1].toInt() }
            }
    }

    private fun String.trimQuotes(): String {
        val start = if (get(0) == '\"') 1 else 0
        val end = if (get(lastIndex) == '\"') lastIndex else length

        return substring(start, end)
    }
}