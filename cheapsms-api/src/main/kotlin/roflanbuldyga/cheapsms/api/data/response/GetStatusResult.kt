package roflanbuldyga.cheapsms.api.data.response

data class GetStatusResult(
    val status: OperationStatus,
    val code: String?,
    val lastCode: String?
)