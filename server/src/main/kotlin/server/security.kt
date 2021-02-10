package server

import HMAC_SECRET
import database.closeSession
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import java.util.Base64
import kotlinx.html.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val ALGORITHM = "HmacSHA256"

val hmacSHA256 = Mac.getInstance(ALGORITHM)!!
val key = SecretKeySpec(HMAC_SECRET.toByteArray(), ALGORITHM)

fun initHMACGenerator() {
    hmacSHA256.init(key)
}

val BASE64_URL_SAFE_ENCODER = Base64.getUrlEncoder()!!

const val SSID_COOKIE_NAME = "SSID_TOKEN"

const val SSID_LENGTH = 64L

fun createSession(userId: String): Cookie {
    val ssid = util.generateRandomHash(SSID_LENGTH)
    database.setSSID(userId.toInt(), ssid)
    return generateSSIDCookie(ssid)
}

fun generateSSIDCookie(ssid: String): Cookie {
    val value = "${generateBase64HMAC(ssid)}.$ssid"
    return sessionCookie(value)
}

fun encodeBase64(message: ByteArray?): String {
    return BASE64_URL_SAFE_ENCODER.encodeToString(message)
}

fun generateBase64HMAC(message: String): String {
    val code = hmacSHA256.doFinal(message.toByteArray())
    return encodeBase64(code)
}

fun ApplicationCall.getSSID(): String? {
    val cookie = request.cookies[SSID_COOKIE_NAME]
    return cookie?.let { verifySession(it) }
}

fun verifySession(cookie: String?): String? {
    if (cookie == null) return null
    val (hmac, ssid) = cookie.split(".").let {
        if (it.size == 2) it else listOf("", "")
    }
    return if (hmac == generateBase64HMAC(ssid) && database.validSSID(ssid)) ssid else null
}

const val ANTI_CSRF_TOKEN_NAME = "__AntiCSRFToken"

fun addAntiCSRFToken(body: BODY, ssid: String?) {
    if (ssid == null) return
    val token = createNewToken(ssid)
    return body.input {
        id = ANTI_CSRF_TOKEN_NAME
        value = token
        style = "display: none;"
    }
}

const val AntiCSRFToken_SIZE = 128L

fun createNewToken(ssid: String): String {
    val token = util.generateRandomHash(AntiCSRFToken_SIZE)
    database.setAntiCSRFToken(ssid, token)
    return token
}

fun verifyAntiCSRFToken(ssid: String, token: String?): Boolean {
    val realToken = database.getAntiCSRFToken(ssid)
    return token != null && realToken == token
}

suspend fun ApplicationCall.deleteSession() {
    respondJson(getSSID()?.let {
        closeSession(it)
        deleteSessionCookie()
        ok(message = "session closed")
    } ?: error("unauthorized"))
}

fun ApplicationCall.deleteSessionCookie() {
    response.cookies.append(sessionCookie("", 0))
}

fun ApplicationCall.createSessionCookie(userId: String) {
    val ssidCookie = createSession(userId)
    response.cookies.append(ssidCookie)
}

fun sessionCookie(value: String, maxAge: Int = COOKIE_MAX_AGE): Cookie {
    return Cookie(
        name = SSID_COOKIE_NAME,
        value = value,
        maxAge = maxAge,
        path = "/",
        httpOnly = true,
        // TODO: 11.12.2020 uncomment
//        secure = true
    )
}