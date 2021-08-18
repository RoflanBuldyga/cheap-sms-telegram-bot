package roflanbuldyga.cheapsms.api.data.response

data class OrderNumberResult(
    val isOk: Boolean,
    val operationId: Long?,
    val number: String?,
    val failureReason: OrderNumberFailureReason?
)

enum class OrderNumberFailureReason {
    NO_NUMBERS,
    NUMBER_OFFLINE,
    NO_BALANCE
}