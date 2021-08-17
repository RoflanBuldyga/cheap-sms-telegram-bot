import com.elbekD.bot.Bot
import com.elbekD.bot.feature.chain.chain
import com.elbekD.bot.types.InlineKeyboardButton
import com.elbekD.bot.types.InlineKeyboardMarkup
import com.elbekD.bot.types.ReplyKeyboard

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
        val storage = base.getById(msg.chat.id)
        storage.replace("message_id", msg.message_id)
        bot.sendMessage(msg.chat.id,
            "Макс гей кста",
            markup = InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton("Получить номер вуша", callback_data = "GetNumber")))))
    }

    bot.onCallbackQuery("GetNumber") {callback ->
        val number = "895268965312"
        val storage = base.getById(callback.from.id.toLong())
        storage.replace("current_number", number)
        bot.editMessageText(callback.from.id.toLong(), storage.get("message_id") as Int, text = "Твой номер: $number")
    }

    bot.start()
}