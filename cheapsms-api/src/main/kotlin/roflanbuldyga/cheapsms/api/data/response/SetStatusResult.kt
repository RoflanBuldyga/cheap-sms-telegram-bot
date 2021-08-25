package roflanbuldyga.cheapsms.api.data.response

enum class SetStatusResult {
    /**
     *  готовность номера подтверждена
     */
    ACCESS_READY,

    /**
     *  ожидание нового смс
     */
    ACCESS_RETRY_GET,

    /**
     *  сервис успешно активирован
     */
    ACCESS_ACTIVATION,

    /**
     *  активация отменена
     */
    ACCESS_CANCEL,

    /**
     * Ошибка сманы статуса еблан блять
     */
    BAD_CHANGE_STATUS
}