import kotlinx.browser.window

// TODO: 12.12.2020 waiting animation

fun registerPage() {
    window.onload = {
        sendAuthRequest("register", arrayOf("reg-input1", "reg-input2", "reg-input3", "reg-input4"))
        addInputEvents()
    }
}