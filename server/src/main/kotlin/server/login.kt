package server

import database.smartCast
import java.lang.NullPointerException

fun processLoginUser(inputs: Map<String, String>): Response {
    val email: String
    val password: String
    try {
        email = inputs["log-input1"]!!
        password = inputs["log-input2"]!!
    } catch (e: NullPointerException) {
        return panic("wrong input types")
    }
    return checkLoginUser(email, password)
}

// TODO: 16.12.2020 remove npe
fun checkLoginUser(email: String, password: String): Response {
    responseOr(checkEmail(email), checkPassword(password)).let { if (it.status != ValidationStatus.OK) return it }
    val user = database.getUserAttrsByEmail(email, "password", "id").map {
        it.key to smartCast(it.key, it.value)
    }.toMap()
    return (user["password"] != null && verifyPassword(user["password"]!!, password)).let {
        if (it) ok(server = user["id"]!!) else error(message = "credentials")
    }
}