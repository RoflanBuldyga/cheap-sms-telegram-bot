package roflanbuldyga.cheapsms.api

data class CheapSMSConfig(
    val debugMode: Boolean,
    val siteUrl: String,
    val api1Url: String,
    val api2Url: String,
)

fun config(debugMode: Boolean) = CheapSMSConfig(
    debugMode = debugMode,
    siteUrl = "https://cheapsms.ru/",
    api1Url = "https://cheapsms.pro/handler/index",
    api2Url = "https://cheapsms.ru/api/"
)