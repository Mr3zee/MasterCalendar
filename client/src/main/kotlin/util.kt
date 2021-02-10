import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLHtmlElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.xhr.XMLHttpRequest

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

val root = (document.getElementsByTagName("html")[0] as HTMLHtmlElement)

fun mainColor(): String {
    return window.getComputedStyle(root).getPropertyValue("--main-color")
}

fun secondColor(): String {
    return window.getComputedStyle(root).getPropertyValue("--second-color")
}

fun badColor(): String {
    return window.getComputedStyle(root).getPropertyValue("--bad-color")
}

fun setRequestEvent(
    url: String,
    btn: String,
    type: String,
    inputs: Array<String>,
    eventGood: (String) -> Unit,
    eventBad: (String) -> Unit
) {
    val btnElement = document.getElementById(btn)!! as HTMLInputElement
    btnElement.onclick = {
        val request = XMLHttpRequest()
        request.open("POST", url)
        request.onload = {
            val response = request.responseJsonMessage(String.serializer())
            val status = response.status
            val message = response.message
            if (status == "OK") {
                eventGood(message)
            } else {
                eventBad(message)
            }
        }
        request.send(jsonifyForm(inputs, type))
    }
}

fun jsonifyForm(inputs: Array<String>, type: String): String {
    val retval = mutableMapOf<String, String>()
    for (i in inputs) {
        (document.getElementById(i)!! as HTMLInputElement).let {
            retval.put(it.id, it.value)
        }
    }
    document.getElementById("__AntiCSRFToken")?.let {
        (it as HTMLInputElement).let { el ->
            retval.put(el.id, el.value)
        }
    }
    return Json.encodeToString(RequestInputs(type, retval))
}

@Serializable
data class RequestInputs(
    val type: String,
    val inputs: Map<String, String>
)

@Serializable
data class Response<T>(
    val status: String,
    val message: T,
)

inline fun <reified T> XMLHttpRequest.responseJsonMessage(serializer: KSerializer<T>): Response<T> {
    return Json.decodeFromString(Response.serializer(serializer), responseText)
}