import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.*
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

// TODO: 04.12.2020 add notification font size detector,
//  manage css vars/integrate with js code (by sass)

// TODO: 06.12.2020 fix blocking header button on confirm delete

val emailForm = SettingsProperty(
    type = "email",
    inputs = arrayOf("input-email"),
)

val nameForm = SettingsProperty(
    type = "name",
    inputs = arrayOf("input-name"),
)

val passwordForm = SettingsProperty(
    type = "password",
    inputs = arrayOf("input-password", "input-repeat-password"),
)

val deleteForm = SettingsProperty(
    type = "delete",
    inputs = arrayOf("input-delete"),
    eventGood = { deleteEventGood() },
)

val confirmDeleteForm = SettingsProperty(
    type = "confirm-delete",
    inputs = arrayOf("input-confirm-delete"),
    eventGood = { window.location.replace("/login") },
    eventBad = { confirmDeleteBadEvent(it) }
)

fun settingsPage() {
    window.onload = {
        setSettings(arrayOf(emailForm, nameForm, passwordForm, deleteForm))
        setLogout()
    }
}

fun setSettings(properties: Array<SettingsProperty>) {
    for ((type, inputs, eventGood, eventBad) in properties) {
        setRequestEvent("/auth", "${unionTypes(type)}-btn", "settings", inputs, eventGood, eventBad)
    }
}

data class SettingsProperty(
    val type: String,
    val inputs: Array<String>,
    val eventGood: (String) -> Unit = { requestAccepted(it) },
    val eventBad: (String) -> Unit = { requestRejected(it) }
)

val promiseMap: HashMap<String, Boolean> = hashMapOf(
    "name" to false,
    "email" to false,
    "password" to false,
    "repeat-password" to false,
    "delete" to false,
    "confirm-delete" to false,
)

fun requestAccepted(type: String) = requestCommon(
    type = type,
    color = secondColor(),
    text = goodNotificationText(type),
    buttonValue = "Submitted"
)

fun requestRejected(type: String) = requestCommon(
    type = type,
    color = badColor(),
    text = badNotificationText(type),
    buttonValue = "Rejected"
)

fun requestCommon(
    type: String,
    color: String,
    text: String,
    buttonValue: String,
    getField: (HTMLElement) -> String = { it.innerHTML },
    setField: (HTMLElement, String) -> Unit = { el, value -> el.innerHTML = value },
): Promise<Any> {
    return requestEvent(type) {
        animateBlock(unionTypes(type), color, text, buttonValue, getField, setField)
    }
}

fun requestEvent(type: String, event: () -> Unit) = GlobalScope.promise {
    if (!promiseMap[type]!!) {
        promiseMap[type] = true
        Promise<Any> { resolve, _ ->
            event()
            resolve(Unit)
        }.await()
        promiseMap[type] = false
    }
}

fun animateBlock(
    type: String,
    color: String,
    text: String,
    buttonValue: String,
    getField: (HTMLElement) -> String,
    setField: (HTMLElement, String) -> Unit,
) {
    colorHeader(type, color)
    showNotification(type, text, getField, setField)
    blockChangeButton(type, buttonValue)
}

fun colorHeader(type: String, color: String) {
    with(document.getElementById("name-container-$type")!! as HTMLDivElement) {
        style.setProperty("animation-name", "")
        getBoundingClientRect()
        style.setProperty("--back-color", color)
        style.setProperty("animation-name", "colorHeader")
    }
}

fun showNotification(
    type: String,
    text: String,
    getField: (HTMLElement) -> String,
    setField: (HTMLElement, String) -> Unit
) = GlobalScope.promise {
    val header = document.getElementById("header-text-$type")!! as HTMLElement
    val old = getField(header)
    changeHeader(header) {
        setField(it, text)
    }.await()
    delay(1000L)
    changeHeader(header) {
        setField(it, old)
    }.await()
}

fun changeHeader(header: HTMLElement, changeText: (HTMLElement) -> Unit) = animate(header) {
    it.close()
}.then {
    changeText(header)
    animate(header) { it.open() }
}

fun animate(element: HTMLElement, animation: (HTMLElement) -> Unit): Promise<Any> {
    return Promise { resolve, _ ->
        animation(element)
        element.addEventListener("transitionend", {
            resolve(Unit)
        }, AddEventListenerOptions().once to true)
    }
}

fun HTMLElement.close() {
    style.width = "${scrollWidth}px"
    getBoundingClientRect()
    style.width = "0"
}

fun HTMLElement.open() {
    style.width = "${scrollWidth}px"
}

fun blockChangeButton(type: String, blockText: String) {
    blockButton(document.getElementById("$type-btn")!! as HTMLInputElement) { btn ->
        buttonShowMessage(btn, blockText)
    }
}

fun blockButton(button: HTMLInputElement, action: (HTMLInputElement) -> Promise<Any>) = GlobalScope.promise {
    val onclick = button.onclick
    button.onclick = { }
    button.style.cursor = "default"
    action(button).await()
    button.style.cursor = "pointer"
    button.onclick = onclick
}

fun buttonShowMessage(button: HTMLInputElement, msg: String) = GlobalScope.promise {
    val old = button.value
    buttonChange(button, msg).await()
    delay(1000L)
    buttonChange(button, old).await()
    Unit
}

fun buttonChange(button: HTMLInputElement, text: String) = animate(button) {
    (it as HTMLInputElement).fadeOut()
}.then {
    button.value = text
    animate(button) { (it as HTMLInputElement).fadeIn() }
}

fun HTMLInputElement.fadeOut() {
    style.opacity = "0"
}

fun HTMLInputElement.fadeIn() {
    style.opacity = "1"
}

fun goodNotificationText(serverMessage: String): String {
    return when (serverMessage) {
        "email" -> "Your email was changed"
        "name" -> "Your account name was changed"
        "password" -> "Your password was changed"
        else -> "Successful!"
    }
}

fun badNotificationText(serverMessage: String): String {
    return when (serverMessage) {
        "email" -> "Wrong email format"
        "name" -> "This name is already taken"
        "password" -> "Password does not meet requirements"
        "repeat-password" -> "Passwords should match"
        "delete" -> "Entered name is wrong"
        "confirm-delete" -> "Wrong Password"
        else -> "Request failed, try again later"
    }
}

fun unionTypes(type: String): String {
    return type.split("-").last()
}

fun deleteEventGood(): Unit = changePage(
    id = "confirm-delete",
    oldPageId = "delete",
    newPageStartLeft = "100%",
    oldPageEndLeft = "-100%",
    getNewHeaderElement = { getConfirmDeleteReturnButton() },
    setNewHeaderText = { (it as HTMLInputElement).value = "Return back" },
    newButtonText = "CONFIRM",
    from = confirmDeleteForm,
    onEnd = {
        val headerButton = document.getElementById("header-text-delete")!! as HTMLInputElement
        headerButton.disabled = false
        headerButton.style.cursor = "pointer"
        headerButton.onclick = {
            returnToDeletePage()
        }
    }
)

fun returnToDeletePage(): Unit = changePage(
    id = "delete",
    oldPageId = "confirm-delete",
    newPageStartLeft = "-100%",
    oldPageEndLeft = "100%",
    getNewHeaderElement = { getOldDeleteHeader() },
    setNewHeaderText = { (it as HTMLSpanElement).innerHTML = "Delete Account" },
    newButtonText = "Delete",
    from = deleteForm,
)


fun changePage(
    id: String,
    oldPageId: String,
    newPageStartLeft: String,
    oldPageEndLeft: String,
    getNewHeaderElement: () -> HTMLElement,
    setNewHeaderText: (HTMLElement) -> Unit,
    newButtonText: String,
    from: SettingsProperty,
    onEnd: () -> Unit = { },
) {
    Promise<Any> { resolve, _ ->
        changeDeleteHeader { getNewHeaderElement() }
        resolve(Unit)
    }.then {
        blockButton(document.getElementById("delete-btn")!! as HTMLInputElement) { button ->
            slidePage(id, oldPageId, newPageStartLeft, oldPageEndLeft).catch {
                println(it)
            }
            changeHeader(document.getElementById("header-text-delete")!! as HTMLElement) { header ->
                setNewHeaderText(header)
            }
            buttonChange(button, newButtonText)
        }
    }.then {
        addPageEvent(from)
        onEnd()
    }
}

fun changeDeleteHeader(newHeader: () -> HTMLElement) {
    val oldHeader = document.getElementById("header-text-delete")!! as HTMLElement
    val parent = oldHeader.parentNode!!
    parent.removeChild(oldHeader)
    parent.appendChild(newHeader())
}

fun getConfirmDeleteReturnButton(): HTMLElement {
    return document.create.input(InputType.button, classes = "header-text") {
        id = "header-text-delete"
        value = "Delete Account"
        disabled = true
        style = "cursor: default;"
    }
}

fun getOldDeleteHeader(): HTMLElement {
    return document.create.span(classes = "header-text") {
        id = "header-text-delete"
        +"Return back"
    }
}

fun slidePage(
    id: String,
    oldPageId: String,
    newPageStartLeft: String,
    oldPageEndLeft: String,
): Promise<Any> {
    return Promise { resolve, _ ->
        val oldPage = document.getElementById("page-$oldPageId")!! as HTMLDivElement
        oldPage.parentNode!!.appendChild(getNewPage(id))

        val newPage = document.getElementById("page-$id")!! as HTMLDivElement
        newPage.style.left = newPageStartLeft
        newPage.getBoundingClientRect()
        changePage(oldPage, newPage, oldPageEndLeft)
        resolve(Unit)
    }
}

fun changePage(oldPage: HTMLDivElement, newPage: HTMLDivElement, oldPageLeft: String) = GlobalScope.promise {
    movePage(oldPage, oldPageLeft)
    movePage(newPage, "0")
    delay(1000L)
    oldPage.parentNode!!.removeChild(oldPage)
}

fun movePage(page: HTMLDivElement, left: String) {
    page.style.left = left
}

fun getNewPage(id: String): HTMLElement {
    return when (id) {
        "delete" -> getDeletePage()
        "confirm-delete" -> getConfirmDeletePage()
        else -> throw IllegalArgumentException("Wrong id")
    }
}

fun getConfirmDeletePage(): HTMLDivElement {
    return getInputForm(
        id = "confirm-delete",
        label = "Enter your password to confirm deletion",
        inputType = InputType.text,
        inputPlaceholder = "Password",
        caption = "After pressing CONFIRM button your account will be immediately deleted"
    )
}

fun getDeletePage(): HTMLDivElement {
    return getInputForm(
        id = "delete",
        label = "Enter your account name to confirm deletion",
        inputType = InputType.text,
        inputPlaceholder = "Account Name",
        caption = "WARNING! This action cannot be undone! Your account will be deleted completely!"
    )
}

fun getInputForm(
    id: String,
    label: String,
    inputType: InputType,
    inputPlaceholder: String,
    caption: String,
): HTMLDivElement {
    return document.create.div(classes = "change-page") {
        this.id = "page-$id"
        div("field-container") {
            div("field") {
                this.id = "f-$id"
                label { +label }
                input {
                    type = inputType
                    this.id = "input-$id"
                    placeholder = inputPlaceholder
                }
                span("caption") { +caption }
            }
        }
    }
}

fun addPageEvent(form: SettingsProperty) {
    setSettings(arrayOf(form))
}

fun confirmDeleteBadEvent(response: String) {
    blockButton(document.getElementById("header-text-delete")!! as HTMLInputElement) {
        requestCommon(
            type = response,
            color = badColor(),
            text = badNotificationText(response),
            buttonValue = "Rejected",
            getField = { (it as HTMLInputElement).value },
            setField = { el, value -> (el as HTMLInputElement).value = value },
        )
    }
}

fun setLogout() {
    val button = document.getElementById("logout")!! as HTMLInputElement
    button.addEventListener("click", {
        val request = XMLHttpRequest()
        request.open("POST", "/logout")
        request.onload = {
            window.location.replace("/login")
        }
        request.send()
    })
}