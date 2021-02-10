import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.*

// TODO: 27.11.2020 make "forgot password" button

// TODO: 08.12.2020 set try limit

// TODO: 15.12.2020 check blank fields

fun loginPage() {
    window.onload = {
        sendAuthRequest("login", arrayOf("log-input1", "log-input2"))
        addInputEvents()
    }
}

fun sendAuthRequest(type: String, inputs: Array<String>) {
    setRequestEvent(
        url = "/auth",
        btn = "button",
        type = type,
        inputs = inputs,
        eventGood = { window.location.replace("/") },
        eventBad = { addErrorMessage(it, type) }
    )
}

fun addErrorMessage(response: String, type: String) {
    val message: String = map[type]?.let { it(response) }!!
    if (document.getElementById("error-block") == null) {
        val body = (document.getElementsByClassName("body-class")[0] as HTMLDivElement)
        body.style.marginLeft =
            "calc(50% - (var(--block-width-$type) + var(--error-block-offset) + var(--error-block-width)) / 2)"
        body.after(createErrorBlock(response.capitalize(), message))

        val errorBlock = (document.getElementById("error-block")!! as HTMLDivElement)
        errorBlock.getBoundingClientRect()
        errorBlock.style.setProperty("--error-opacity", "1")
        errorBlock.style.setProperty("transform", "scale(1.02)")
    } else {
        val errorBlock = (document.getElementById("error-block")!! as HTMLDivElement)
        val errorHeader = (document.getElementById("error-name")!! as HTMLSpanElement)
        val errorText = (document.getElementById("error-msg")!! as HTMLSpanElement)

        errorHeader.style.setProperty("opacity", "var(--error-text-opacity)")
        errorText.style.setProperty("opacity", "var(--error-text-opacity)")

        errorHeader.style.setProperty("transition", "opacity .2s linear")
        errorText.style.setProperty("transition", "opacity .2s linear")

        errorBlock.style.setProperty("--error-text-opacity", "0")

        errorHeader.addEventListener("transitionend", {
            errorHeader.innerHTML = response.capitalize()
            errorText.innerHTML = message
            errorBlock.style.setProperty("--error-text-opacity", "1")
        }, { AddEventListenerOptions().once to true })
    }
}

fun createErrorBlock(header: String, message: String): HTMLElement {
    return document.create.div {
        id = "error-block"
        div(classes = "error-header") {
            span {
                id = "error-header"
                +"Error:"
            }
            span {
                id = "error-name"
                +header
            }
        }
        div(classes = "error-body") {
            span {
                id = "error-msg"
                +message
            }
        }
    }
}

val map: HashMap<String, (String) -> String> = hashMapOf(
    "login" to { "Invalid login or password. Please check your credentials and try again or try to recover your password" },
    "register" to {
        when (it) {
            "name" -> "This name is already taken. Please, choose another one, as every name should by unique"
            "email" -> "Invalid email syntax. Please, check your input, it should look like example@mail.com"
            "password" -> "Password should be at least 12 symbols, " +
                    "containing lower and upper case letters, digits and special characters: @\\$!%*?&"
            "repeat-password" -> "Passwords do not match. Please, type in correct password and try again"
            else -> "Unknown error occurred"
        }
    }
)

fun addInputEvents() {
    val elements = document.getElementsByClassName("form-input")
    val labels = document.getElementsByClassName("form-label")
    for (i in 0 until elements.length) {
        val input = elements.item(i)!! as HTMLInputElement
        input.addEventListener("focusout", {
            val label = labels.item(i)!! as HTMLLabelElement
            if (input.value == "") {
                label.removeClass("above")
                label.addClass("inside")
            }
        })
        input.addEventListener("focus", {
            val label = labels.item(i)!! as HTMLLabelElement
            label.removeClass("inside")
            label.addClass("above")
        })
    }
}