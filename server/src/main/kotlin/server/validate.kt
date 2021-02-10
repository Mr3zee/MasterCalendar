package server

import io.ktor.application.*
import io.ktor.request.*

enum class ValidationStatus {
    OK,
    ERROR,
    FATAL,
}

// TODO: 08.12.2020 make validation async

// TODO: 09.12.2020 add google auth 

suspend fun ApplicationCall.processRequest() {
    val jsonForm = receive<String>()
    var needToCreateSession = false
    val ssid = getSSID()
    val form = decodeRequest<Form>(jsonForm)
    val antiCSRFToken = form.inputs[ANTI_CSRF_TOKEN_NAME]

    if (ssid == null || !verifyAntiCSRFToken(ssid, antiCSRFToken)) {
        if (form.type != "login" && form.type != "register") respondJson(error("Unauthorized request"))
        needToCreateSession = true
    }

    val processStatus = processForm(ssid, form.type, form.inputs)

    val serverMessage = processStatus.server.toString()

    if (processStatus.status == ValidationStatus.FATAL) {
        processFatal(serverMessage)
    }

    if (processStatus.status == ValidationStatus.OK) {
        if (needToCreateSession || form.type == "login" || form.type == "register") {
            createSessionCookie(serverMessage)
        } else if (serverMessage == "delete session") {
            deleteSessionCookie()
        }
        // TODO: 12.12.2020 fix messages -> user/server messages
        processStatus.server = ""
    }
    respondJson(processStatus)
}

fun processForm(ssid: String?, type: String, inputs: Map<String, String>): Response {
    return when(type) {
        "settings" -> processSettingsUser(ssid!!, inputs)
        "login" -> processLoginUser(inputs)
        "register" -> processRegisterUser(inputs)
        else -> error(message = "wrong request type")
    }
}
