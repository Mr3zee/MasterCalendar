package server

// TODO: 14.12.2020 inform user about restrictions

fun checkName(name: String): Response {
    if (validName(name)) return ok()
    return error(message = "name")
}

val nameRegEx = Regex("^[A-Za-z\\d]{4,25}$")

fun validName(name: String): Boolean {
    return nameRegEx.matches(name)
}

fun checkEmail(email: String): Response {
    if (validEmail(email)) return ok()
    return error(message = "email")
}

val emailRegEx = Regex("^[\\w!#$%&'*+/=?`{|}~^-]+(?:.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+.)+[a-zA-Z]{2,6}$")

fun validEmail(email: String): Boolean {
    return emailRegEx.matches(email)
}

val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$")

fun checkPassword(password: String): Response {
    if (passwordRegex.matches(password)) return ok()
    return error(message = "password")
}

fun checkRepeatPassword(password: String, repeatPassword: String?): Response {
    if (password != repeatPassword) return error(message = "repeat-password")
    return ok()
}

val dateREgEx = Regex("(^(((0[1-9]|1[0-9]|2[0-8])[/](0[1-9]|1[012]))|((29|30|31)[/](0[13578]|1[02]))|((29|30)[/](0[469]|11)))[/](19|[2-9][0-9])\\d\\d$)|(^29[/]02[/](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)")

fun validateDate(date: String): Boolean {
    return dateREgEx.matches(date)
}