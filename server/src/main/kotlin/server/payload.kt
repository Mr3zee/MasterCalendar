package server

import database.smartCast
import io.ktor.application.*
import io.ktor.request.*
import java.lang.IllegalArgumentException

const val COOKIE_MAX_AGE = 3 * 31 * 24 * 3600

suspend fun ApplicationCall.respondPayload() {
    val attrs = decodeRequest<Array<String>>(receive())
    val values = getSSID()?.let {
        getUserAttrs(it, *attrs)
    }
    respondJson(values?.let { ok(message = values) } ?: error(message = "Invalid attrs requested"))
}

fun getUserAttrs(ssid: String, vararg attrs: String): Map<String, String?> {
    return database.getUserAttrs(ssid, *attrs).map { it.key to smartCast(it.key, it.value) }.toMap()
}