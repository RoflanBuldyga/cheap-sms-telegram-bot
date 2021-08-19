import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.feature.chain.jumpTo
import com.elbekD.bot.feature.chain.jumpToAndFire
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import roflanbuldyga.cheapsms.api.CheapSMSBlockingClient
import roflanbuldyga.cheapsms.api.enums.Services
import roflanbuldyga.cheapsms.api.exception.CheapSMSApiException
import data.Number
import roflanbuldyga.cheapsms.api.data.response.OperationStatus
import roflanbuldyga.cheapsms.api.exception.CheapSMSOrderException
import kotlin.random.Random

/**
 *
 * @author Aydar Rafikov
 */
fun main() {
    val token = Config.TOKEN
    val username = Config.USERNAME
    val bot = Bot.createPolling(username, token)
    val base = BaseEmulator()
    val client = CheapSMSBlockingClient()

    bot.chain("/start") { msg ->
        val text = "Привет, отправь мне свой токен от CheapSMS"
        bot.sendMessage(msg.chat.id, text)
    }.then("getting_cheapsms_token") { msg ->
        val cheapsmsToken = msg.text!!
        try {
            client.getBalance(cheapsmsToken)
        } catch (ex: CheapSMSApiException) { // todo catch useful because exception trows again (why??????)
            val text = "Токен не принят, введи рабочий, блять"
            bot.sendMessage(msg.chat.id, text)
            bot.jumpTo("getting_cheapsms_token", msg)
        }

        val storage = base.getById(msg.chat.id)
        storage.replace("token", cheapsmsToken)
        bot.jumpToAndFire("menu_show", msg)
    }.then("menu_show") { msg ->
        val storage = base.getById(msg.chat.id)
        val cheapsmsToken = storage["token"].toString()
        val balance = getBalance(cheapsmsToken)
        val countWhoosh = getWhooshNumberCount(cheapsmsToken)
        val text = startText(balance, countWhoosh)
        val markup = if (countWhoosh != 0) getOrderKeyboard() else getUpdateKeyboard()
        val message = bot.sendMessage(msg.chat.id, text, markup = markup)
        storage.replace("message_id", message.get().message_id)
    }.build()

    fun acceptSMS(chatId: Long, number: Number) {
        val storage = base.getById(chatId)
        val cheapsmsToken = storage["token"].toString()
        var time = 2*60
        while (time > 0 && storage["current_number"] == number) {
            Thread.sleep(1000 * 5)
            time -= 5
            val status = client.getStatus(cheapsmsToken, number.id)
            if (status.status == OperationStatus.STATUS_OK) {
                val message = bot.sendMessage(chatId, status.code!!).get()
                storage.replace("code_message_id", message.message_id)
                break
            }
        }
    }

    fun orderNewNumberLogic(callback: CallbackQuery) {
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val cheapsmsToken = storage["token"].toString()
        var strNumber: String
        var number: Number? = null
        try {
            number = orderNumber(cheapsmsToken)
            strNumber = number.number
            storage.replace("current_number", number)
        } catch (ex: CheapSMSOrderException) {
            strNumber = ex.message!!
        }
        val messageId = storage["message_id"] as Int
        val messageText = "Твой номер: ${strNumber.replace("+7", "+7 ")}\n" +
                "Статус: ожидание СМС"
        bot.editMessageText(chatId, messageId , text = messageText, markup = getReOrderKeyboard())

        if (number != null)
            acceptSMS(chatId, number)
    }

    bot.onCallbackQuery("OrderNumber") {callback -> // todo enum on callback data
        orderNewNumberLogic(callback)
    }

    bot.onCallbackQuery("ReOrderNumber") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val number = storage["current_number"]
        if (number != null) {
            cancelOrderNumber(number as Number)
        }
        if (storage["code_message_id"] != null) {
            bot.deleteMessage(chatId, storage["code_message_id"] as Int)
        }

        orderNewNumberLogic(callback)
    }

    bot.onCallbackQuery("CancelOrderNumber") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)

        val number = storage.get("current_number")
        val messageId = storage.get("message_id") as Int
        val cheapsmsToken = storage.get("token").toString()
        val balance = getBalance(cheapsmsToken)
        val countWhoosh = getWhooshNumberCount(cheapsmsToken)
        val text = startText(balance, countWhoosh)
        val markup = if (countWhoosh != 0) getOrderKeyboard() else getUpdateKeyboard()
        if (number != null) {
            cancelOrderNumber(number as Number)
        }

        // deleting code if sent
        if (storage["code_message_id"] != null) {
            bot.deleteMessage(chatId, storage["code_message_id"] as Int)
        }

        bot.editMessageText(chatId, messageId, text = text, markup = markup)
    }

    bot.onCallbackQuery("UpdateNumbers") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val cheapsmsToken = storage.get("token").toString()
        val balance = getBalance(cheapsmsToken)
        val countWhoosh = getWhooshNumberCount(cheapsmsToken)
        val text = startText(balance, countWhoosh)
        val messageId = storage.get("message_id") as Int
        val markup = if (countWhoosh != 0) getOrderKeyboard() else getUpdateKeyboard()
        bot.editMessageText(chatId, messageId, text = text, markup = markup)
    }

    bot.start()
    print("Started")
}

fun getUpdateKeyboard(): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton("Обновить наличие номеров", callback_data = "UpdateNumbers"))))
}

fun cancelOrderNumber(number: Number) {
    // todo заглушка
}

fun getOrderKeyboard(): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton("Получить номер вуша", callback_data = "OrderNumber"))))
}

fun getReOrderKeyboard(): InlineKeyboardMarkup {
    return InlineKeyboardMarkup(listOf(
        listOf(InlineKeyboardButton("Получить новый номер", callback_data = "ReOrderNumber")),
        listOf(InlineKeyboardButton("Я передумал, отменяй заказ блять", callback_data = "CancelOrderNumber"))
        ))
}

fun orderNumber(cheapsmsToken: String): Number {
    val order = CheapSMSBlockingClient().orderNumber(cheapsmsToken, Services.WHOOSH.serviceName)
    if (order.isOk) {
        return Number(order.number!!, order.operationId!!)
    } else {
        throw CheapSMSOrderException(order.failureReason!!)
    }
}

fun getWhooshNumberCount(cheapsmsToken: String): Int {
    return CheapSMSBlockingClient().getServices(cheapsmsToken)[Services.WHOOSH.serviceName]!!
}

fun getBalance(cheapsmsToken: String): String {
    return "${CheapSMSBlockingClient().getBalance(cheapsmsToken)} рублей"
}

fun startText(balance: String, count: Int): String {
    return "Ваш баланс: ${balance}\n" +
           "Осталось номеров Вуша: $count"
}