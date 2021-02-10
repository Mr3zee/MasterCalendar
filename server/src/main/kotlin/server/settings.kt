package server

import java.lang.IllegalArgumentException

const val EMAIL = "input-email"
const val NAME = "input-name"
const val PASSWORD = "input-password"
const val REPEAT_PASSWORD = "input-repeat-password"
const val DELETE = "input-delete"
const val CONFIRM_DELETE = "input-confirm-delete"

fun processSettingsUser(ssid: String, inputs: Map<String, String>): Response {
    for (i in arrayOf(NAME, EMAIL, PASSWORD, DELETE, CONFIRM_DELETE)) {
        inputs[i]?.let { return mapSettings(ssid, i, inputs) }
    }
    throw IllegalArgumentException("Invalid settings input")
}


// TODO: 12.12.2020 make pretty
fun mapSettings(ssid: String, name: String, inputs: Map<String, String>): Response {
    return when (name) {
        NAME -> changeName(ssid, inputs[NAME]!!)
        EMAIL -> changeEmail(ssid, inputs[EMAIL]!!)
        PASSWORD -> changePassword(ssid, inputs[PASSWORD]!!, inputs[REPEAT_PASSWORD])
        DELETE -> deleteAccount(ssid, inputs[DELETE]!!)
        CONFIRM_DELETE -> confirmDeleteAccount(ssid, inputs[CONFIRM_DELETE]!!)
        else -> throw IllegalArgumentException("Invalid settings input")
    }
}

fun changeEmail(ssid: String, email: String): Response {
    checkEmail(email).let { if (it.status == ValidationStatus.ERROR) return it }
    database.setEmail(ssid, email)
    sendEmailConfirmation(email)
    return ok(message = "email")
}

fun changePassword(ssid: String, newPassword: String, repeatPassword: String?): Response {
    responseOr(
        checkPassword(newPassword),
        checkRepeatPassword(newPassword, repeatPassword)
    ).let {
        if (it.status == ValidationStatus.ERROR) return it
    }
    database.setPassword(ssid, newPassword)
    return ok(message = "password")
}

fun changeName(ssid: String, name: String): Response {
    checkName(name).let { if (it.status == ValidationStatus.ERROR) return it }
    database.setAccountName(ssid, name)
    return ok(message = "name")
}

fun deleteAccount(ssid: String, name: String): Response {
    if (database.getUserAttr(ssid, "name") == name) return ok(message = "delete")
    return error(message = "delete")
}

fun confirmDeleteAccount(ssid: String, password: String): Response {
    val dbPassword = database.getUserAttr(ssid, "password") ?: throw IllegalArgumentException("ssid confirm delete")
    if (verifyPassword(dbPassword as String, password)) {
        database.deleteAccount(ssid)
        return ok(message = "confirm-delete", server = "delete session")
    }
    return error(message = "confirm-delete")
}
