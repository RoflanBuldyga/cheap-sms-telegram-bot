package roflanbuldyga.cheapsms.api.exception

import roflanbuldyga.cheapsms.api.data.response.ErrorResponse

open class CheapSMSException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
}

class CheapSMSApiException(
    val error: ErrorResponse,
    val callUrl: String
) : CheapSMSException("Api call [$callUrl] respond with error \"$error\"")

class CheapSMSResponseParsingException(response: String, throwable: Throwable) : CheapSMSException(
    "An unexpected parsing error.\nresponse: $response",
    throwable
)