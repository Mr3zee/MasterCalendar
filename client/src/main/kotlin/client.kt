import kotlinx.browser.window

fun main() {
    onloadPageByName(window.location.pathname)
}

fun onloadPageByName(pageName: String) {
    when (getPageJSFileName(pageName)) {
        "" -> mainPage()
        "login" -> loginPage()
        "register" -> registerPage()
        "settings" -> settingsPage()
        else -> notFoundPage()
    }
}

fun getPageJSFileName(path: String): String {
    return path.split("/").mapIndexed { i, a -> if (i == 1) a else a.capitalize() }.joinToString("")
}