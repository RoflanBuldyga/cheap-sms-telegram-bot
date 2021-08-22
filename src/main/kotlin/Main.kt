import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.ReplyKeyboard
import java.sql.Time
import java.util.*
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

    bot.onCommand("/start") { msg, _ ->
        val storage = base.getById(msg.chat.id) // Initialize base
        val balance = getBalance()
        val countWhoosh = getWhooshNumberCount()
        val text = startText(balance, countWhoosh)
        val markup = if (countWhoosh != 0) getOrderKeyboard() else getUpdateKeyboard()
        val message = bot.sendMessage(msg.chat.id, text, markup = markup)
        storage.replace("message_id", message.get().message_id)
    }

    fun orderNewNumberLogic(callback: CallbackQuery) {
        val number = getNumber()
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val messageId = storage.get("message_id") as Int
        val messageText = "Твой номер: $number\n" +
                "Статус: ожидание СМС"
        storage.replace("current_number", number)
        bot.editMessageText(chatId, messageId , text = messageText, markup = getReOrderKeyboard())
        // todo need waiting sms function
    }

    bot.onCallbackQuery("OrderNumber") {callback -> // todo enum on callback data
        orderNewNumberLogic(callback)
    }

    bot.onCallbackQuery("ReOrderNumber") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val number = storage.get("current_number").toString()
        cancelOrderNumber(number)

        orderNewNumberLogic(callback)
    }

    bot.onCallbackQuery("CancelOrderNumber") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val number = storage.get("current_number").toString()
        val messageId = storage.get("message_id") as Int
        val balance = getBalance()
        val countWhoosh = getWhooshNumberCount()
        val text = startText(balance, countWhoosh)
        val markup = if (countWhoosh != 0) getOrderKeyboard() else getUpdateKeyboard()
        cancelOrderNumber(number)

        bot.editMessageText(chatId, messageId, text = text, markup = markup)
    }

    bot.onCallbackQuery("UpdateNumbers") {callback ->
        val chatId = callback.from.id.toLong()
        val storage = base.getById(chatId)
        val balance = getBalance()
        val countWhoosh = getWhooshNumberCount()
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

fun cancelOrderNumber(number: String) {
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

fun getNumber(): String {
    val randNum = Random.nextInt(9990000000.toInt(), 9999999999.toInt())
    return "+7$randNum"
}

fun getWhooshNumberCount(): Int {
    return Random.nextInt(0, 5)
}

fun getBalance(): String {
    return "5 рублей"
}

fun startText(balance: String, count: Int): String {
    return "Ваш баланс: ${balance}\n" +
           "Осталось номеров Вуша: $count"
}