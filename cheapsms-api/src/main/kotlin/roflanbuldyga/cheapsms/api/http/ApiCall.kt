package roflanbuldyga.cheapsms.api.http

import roflanbuldyga.cheapsms.api.data.response.ErrorResponse

data class ApiCall<Response> internal constructor(
    val params: Map<String, String>,
    val responseParser: ResponseParser<Response>
) {
    companion object {
        fun <Response> create(
            responseParser: ResponseParser<Response>,
            apiKey: String,
            action: String,
            params: Map<String, String>
        ): ApiCall<Response> = ApiCall(
            params = HashMap(params).apply {
                put("api_key", apiKey)
                put("action", action)
            },
            responseParser = responseParser
        )
    }
}

abstract class ResponseParser<Response> {
    abstract fun parseData(response: String): Response

    fun parseError(response: String): ErrorResponse? {
        return parseEnum<ErrorResponse>(response)
    }

    protected inline fun <reified E: Enum<E>> parseEnum(response: String): E? {
        return enumValues<E>().firstOrNull { response.startsWith(it.name) }
    }

    protected inline fun parseParts(response: String, block: (parts: List<String>) -> Unit) {
        block.invoke(response.split(':'))
    }
}