package roflanbuldyga.cheapsms.api.data.response

enum class OperationStatus {
    /**
     *  ожидание смс
     */
    STATUS_WAIT_CODE,

    /**
     * :$lastcode - ожидание уточнения кода (где $lastcode - прошлый, неподошедший код)
     */
    STATUS_WAIT_RETRY,

    /**
     * ожидание повторной отправки смс
     */
    STATUS_WAIT_RESEND,

    /**
     * активация отменена
     */
    STATUS_CANCEL,

    /**
     * :$code - код получен (где $code - код активации)
     */
    STATUS_OK,
}