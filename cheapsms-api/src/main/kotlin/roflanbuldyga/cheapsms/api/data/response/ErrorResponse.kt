package roflanbuldyga.cheapsms.api.data.response

enum class ErrorResponse {
    /**
     * некорректное действие
     */
    BAD_ACTION,

    /**
     * неверный API-ключ
     */
    BAD_KEY,

    /**
     * некорректное наименование сервиса
     */
    BAD_SERVICE,

    /**
     * некорректный статус
     */
    BAD_STATUS,

    /**
     * id активации не существует
     */
    NO_ACTIVATION,

    /**
     * номер старой операции не найден
     */
    ORDER_NOT_FOUND,

    /**
     * невозможно повторить этот заказ
     */
    IMPOSSIBLE_AGAIN,

    /**
     * ошибка SQL-сервера
     */
    ERROR_SQL,
}