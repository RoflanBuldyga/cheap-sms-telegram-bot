package roflanbuldyga.cheapsms.api.http

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import roflanbuldyga.cheapsms.api.exception.CheapSMSApiException
import roflanbuldyga.cheapsms.api.exception.CheapSMSResponseParsingException

class ApiHttpClient(val apiUrl1: String, debugMode: Boolean) {
    private val httpClient = HttpClient {
        install(UserAgent) {
            agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36"
        }

        if (debugMode) {
            install(Logging) {
                level = LogLevel.BODY
            }
        }
    }

    suspend fun <Response> executeApiCall(apiCall: ApiCall<Response>): Response = coroutineScope {
        val response: HttpResponse = httpClient.get(apiUrl1) {
            apiCall.params.forEach { parameter(it.key, it.value) }
        }

        val responseText = response.readText()

        apiCall.responseParser.parseError(responseText)?.let { error ->
            throw CheapSMSApiException(error, response.request.url.toString())
        }

        try {
            apiCall.responseParser.parseData(responseText)
        } catch (ex: Exception) {
            throw CheapSMSResponseParsingException(responseText, ex)
        }
    }

    fun close() {
        httpClient.close()
    }
}