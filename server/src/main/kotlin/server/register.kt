package server

import java.lang.NullPointerException

fun processRegisterUser(inputs: Map<String, String>): Response {
    val name: String;
    val email: String;
    val password: String;
    val repeatPassword: String;
    try {
        name = inputs["reg-input1"]!!
        email = inputs["reg-input2"]!!
        password = inputs["reg-input3"]!!
        repeatPassword = inputs["reg-input4"]!!
    } catch (e: NullPointerException) {
        return panic("wrong input names")
    }
    val check = checkRegisterUser(name, email, password, repeatPassword)
    if (check.status != ValidationStatus.OK) return check
    return addUser(name, email, password)
}

fun checkRegisterUser(name: String, email: String, password: String, repeatPassword: String): Response {
    return responseOr(
        checkName(name),
        checkEmail(email),
        checkPassword(password),
        checkRepeatPassword(password, repeatPassword)
    )
}

fun addUser(name: String, email: String, password: String): Response {
    if (!database.isUniqueName(name)) return error(message = "name")
    if (!database.isUniqueEmail(email)) return error(message = "email")
    val hashPassword = toHashPassword(password)
    val id = database.addUser(name, email, hashPassword)
    sendEmailConfirmation(email)
    return ok(server = id)
}
