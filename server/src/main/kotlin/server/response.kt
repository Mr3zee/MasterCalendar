package server

import com.fasterxml.jackson.module.kotlin.*
import html.HTMLPage
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import java.lang.IllegalArgumentException

val mapper = jacksonObjectMapper()

// TODO: 14.12.2020 make error handler

suspend fun ApplicationCall.redirectPage(location: String, permanent: Boolean = true) {
    respondRedirect(permanent = permanent) {
        protocol = URLProtocol.HTTP
        host = request.host()
        port = request.port()
        encodedPath = location
    }
}

suspend fun ApplicationCall.authRedirect() {
    redirectPage("/login", false)
}

suspend fun ApplicationCall.homeRedirect() {
    redirectPage("/", false)
}

suspend fun ApplicationCall.showProtectedPage(page: HTMLPage) {
    getSSID()?.let { showPage(page, it) } ?: authRedirect()
}

suspend fun ApplicationCall.showAuthPage(page: HTMLPage) {
    getSSID()?.let { homeRedirect() } ?: showPage(page)
}

suspend fun ApplicationCall.showPage(page: HTMLPage, ssid: String? = null) {
    respondHtml {
        page.html(this, ssid = (ssid ?: getSSID()))
    }
}

suspend fun ApplicationCall.respondJson(response: Response, status: HttpStatusCode = HttpStatusCode.OK) {
    respond(status, encodeResponse(response))
}

suspend fun ApplicationCall.processPOSTCalendar() {
    getSSID()?.let {
        val request = decodeRequest<DateRequest>(receive())
        respondJson(if (database.changeDate(it, request.date, request.state)) ok() else error())
    } ?: authRedirect()
}

suspend fun ApplicationCall.getCalendar() {
    getSSID()?.let { ssid ->
        respondJson(ok(message = database.getDates(ssid)))
    } ?: authRedirect()
}

fun error(server: Any = "", message: Any = ""): Response {
    return Response(ValidationStatus.ERROR, server, message)
}

fun ok(server: Any = "", message: Any = ""): Response {
    return Response(ValidationStatus.OK, server, message)
}

fun panic(server: Any = ""): Response {
    return Response(ValidationStatus.FATAL, server, "")
}

fun processFatal(message: String) {
    throw IllegalArgumentException("FATAL SERVER ERROR: $message")
}

fun responseOr(vararg args: Response): Response {
    for(i in args) {
        if (i.status != ValidationStatus.OK) return i
    }
    return args.run { if (size > 0) get(0) else ok("", "") }
}

data class DateRequest(
    val calendarName: String,
    val date: String,
    val state: Boolean,
)

data class Form(
    val type: String,
    val inputs: Map<String, String>,
)

data class Response(
    val status: ValidationStatus = ValidationStatus.OK,
    var server: Any,
    var message: Any,
) {
    fun toUserResponse(): UserResponse {
        return UserResponse(status = status, message = message)
    }
}

data class UserResponse(
    val status: ValidationStatus,
    var message: Any,
)

inline fun <reified T> decodeRequest(request: String): T {
    return mapper.readValue(request)
}

fun encodeResponse(response: Response): String {
    return mapper.writeValueAsString(response.toUserResponse())
}