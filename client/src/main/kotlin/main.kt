import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.*
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.json

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// TODO: 08.12.2020 make months feed frame to load and unload unused ones
//  fix my lists hover
//  add ability to change table name
//  add ability to change calendar name
//  add ability to change month by side button
//  add ability to create new tables / delete old
//  add ability to create new calendars / delete old
//  add ability to share calendars
//  add ability to change calendar picture
//  make month feed infinite

fun mainPage() {
    window.onload = {
        setHideShowButton()
        requestTables()
        requestName()
    }
}

fun headerHeight(): String {
    return window.getComputedStyle(root).getPropertyValue("--header-height")
}

fun setHideShowButton() {
    val button = document.getElementById("hide-show-button")!! as HTMLInputElement
    button.onclick = {
        val hidden = button.style.getPropertyValue("--button-hidden") == "true"
        hideInfoBar(hidden)
        expandMainBlock(hidden)
        if (!hidden)
            changeButtonText(button, "Show Sidebar")
        else
            changeButtonText(button, "Hide Sidebar")
        button.style.setProperty("--button-hidden", "${!hidden}")
    }
}

fun expandMainBlock(hidden: Boolean) {
    with(document.getElementById("main") as HTMLDivElement) {
        style.width = if (!hidden) "100%" else "calc(100% - var(--sidebar-width) - var(--sidebar-offset))"
    }
}

fun hideInfoBar(hidden: Boolean) {
    fun hiddenInfoBarWidth(style: CSSStyleDeclaration): String {
        return "calc(${headerHeight()} - ${style.getPropertyValue("height")} + var(--button-height))"
    }
    with(document.getElementById("info-bar")!! as HTMLDivElement) {
        style.top = if (hidden) headerHeight() else hiddenInfoBarWidth(window.getComputedStyle(this))
    }
}

fun changeButtonText(button: HTMLInputElement, newText: String) {
    blockButton(button) {
        buttonChange(it, newText)
    }
}

data class Table(
    val name: String,
    val months: List<Month>
)

data class Month(
    val date: NormalDate,
    val days: List<Day>
)

data class Day(
    val index: Int,
    var chosen: Boolean,
)

data class NormalDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    override fun toString(): String {
        return "${getDay()}/${getMonth()}/$year"
    }

    fun getMonth(): String {
        return if (month < 10) "0$month" else month.toString()
    }

    fun getDay(): String {
        return if (day < 10) "0$day" else day.toString()
    }

    fun getShortMonth(): String {
        return when (month) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            else -> "Dec"
        }
    }
}

fun addTables(tables: List<Table>) {
    val container = document.getElementById("main-container")!! as HTMLDivElement
    for (table in tables) {
        addTable(container, table)
        tablesSizes[table.name] = table.months.size
        tableMoving[table.name] = false
    }
}

fun addTable(container: HTMLDivElement, table: Table) {
    container.appendChild(createTable(table))
    addOnclickDayEvent(tableName = table.name)
    addOnclickArrowEvents(tableName = table.name)
}

fun createTable(table: Table): HTMLDivElement {
    val name = table.name
    val defDate = table.months[0].date
    return document.create.div(classes = "cell") {
        id = "cell-$name"
        div(classes = "cell-control") {
            div(classes = "cell-name") {
                span { +name }
            }
            div(classes = "control-border")
            div(classes = "cell-date") {
                span {
                    id = "$name-cell-date"
                    +getMonth(defDate)
                }
            }
            div(classes = "control-border")
            div(classes = "cell-arrows") {
                div(classes = "cell-container") {
                    // TODO: 07.12.2020 fix font scale issues
                    input(classes = "arrow $name-arrow") {
                        id = "$name-up-arrow"
                        type = InputType.button
                        value = "Up"
                    }
                    input(classes = "arrow $name-arrow") {
                        id = "$name-down-arrow"
                        type = InputType.button
                        value = "Down"
                    }
                }
            }
        }
        div(classes = "months-feed") {
            id = "$name-month-feed"
            table.months.map { month ->
                div(classes = "month") {
                    id = "$name-${month.date.getMonth()}-${month.date.year}-month"
                    val offset = makeDaysOffset(this, month.date)
                    month.days.map { day ->
                        val color = if (day.chosen) secondColor() else mainColor()
                        this.div(classes = "day") {
                            style = "background: $color;"
                            input(classes = "day-button $name-day-button") {
                                id = NormalDate(month.date.year, month.date.month, day.index).toString()
                                type = InputType.button
                                value = "${day.index}"
                            }
                        }
                    }
                    addBlank(this, 42 - (offset + month.days.size))
                }
            }
        }
    }
}

fun makeDaysOffset(div: DIV, month: NormalDate): Int {
    val offset = calcOffset(month)
    addBlank(div, offset)
    return offset
}

fun calcOffset(month: NormalDate): Int {
    val date = Date(year = month.year, month = month.month - 1, day = month.day)
    var offset = (date.getDay() - 1)
    if (offset < 0) offset = 6
    return offset
}

fun addBlank(div: DIV, count: Int) {
    for (i in 0 until count) {
        div.div(classes = "blank-day")
    }
}

fun getMonth(date: NormalDate): String {
    return "${date.getShortMonth()}, ${date.year}"
}

fun addOnclickDayEvent(tableName: String) {
    val days = document.getElementsByClassName("$tableName-day-button")
    for (i in 0 until days.length) {
        val day = (days[i]!! as HTMLInputElement)
        day.onclick = {
            dayOnclickEvent(day, tableName)
        }
    }
}

fun dayOnclickEvent(day: HTMLInputElement, tableName: String) {
    val date = day.id
    val newState = !getChosen(date)
    val request = XMLHttpRequest()
    request.open("POST", "/calendar")
    request.onload = {
        with((day.parentElement as HTMLDivElement).style) {
            setChosen(date, newState)
            background = if (newState) { secondColor() } else { mainColor() }
        }
    }
    request.send(
        JSON.stringify(
            json(
                "calendarName" to tableName,
                "date" to date,
                "state" to newState
            )
        )
    )
}

fun addOnclickArrowEvents(tableName: String) {
    val arrows = document.getElementsByClassName("$tableName-arrow")
    // TODO: 08.12.2020 fix "$tableName-1-2021-month"
    val topMonth = document.getElementById("$tableName-01-2021-month")!! as HTMLDivElement
    val monthFeed = document.getElementById("$tableName-month-feed")!! as HTMLDivElement
    monthFeed.onwheel = {
        if (it.deltaY > 0) moveDown(topMonth, tableName) else moveUp(topMonth, tableName)
    }
    for (i in 0 until arrows.length) {
        val arrow = (arrows[i]!! as HTMLInputElement)
        val type = arrow.id.split("-")[1]
        arrow.onclick = {
            if (type == "up") moveUp(topMonth, tableName) else moveDown(topMonth, tableName)
        }
    }
}

// TODO: 08.12.2020 remove this one
val tablesSizes: HashMap<String, Int> = hashMapOf()

val tableMoving: HashMap<String, Boolean> = hashMapOf()

fun moveUp(topMonth: HTMLDivElement, tableName: String) = moveMonthFeed(topMonth, -1, tableName) {
    it >= 0
}

fun moveDown(topMonth: HTMLDivElement, tableName: String) = moveMonthFeed(topMonth, 1, tableName) {
    it < tablesSizes[tableName]!!
}

fun moveMonthFeed(
    topMonth: HTMLDivElement,
    add: Int, tableName:
    String, condition: (Int) -> Boolean
) = GlobalScope.promise {
    val tableHeight = getTableHeight()
    val index = getCurrentMonthIndex(topMonth, tableHeight)
    if (condition(index + add) && !tableMoving[tableName]!!) {
        tableMoving[tableName] = true
        setMonth(topMonth, index + add, tableHeight, tableName).await()
        tableMoving[tableName] = false
    }
}

fun getTableHeight(): Int {
    return window.getComputedStyle(
        (document.getElementsByClassName("cell")[0]!! as HTMLDivElement)
    ).height.dropLast(2).toInt()
}

fun getCurrentMonthIndex(month: HTMLDivElement, tableHeight: Int): Int {
    return try {
        val marginTop = window.getComputedStyle(month).getPropertyValue("margin-top").dropLast(2).toInt()
        marginTop / -(tableHeight - 11)
    } catch (e: NumberFormatException) {
        0
    }
}

fun setMonth(topMonth: HTMLDivElement, monthIndex: Int, tableHeight: Int, name: String): Promise<Any> {
    val cellDate = document.getElementById("$name-cell-date")!! as HTMLSpanElement
    return Promise { resolve, _ ->
        // TODO: 08.12.2020 fix this
        cellDate.innerHTML = getMonth(NormalDate(2021, monthIndex, 1))
        topMonth.style.marginTop = "calc(-${monthIndex} * ${tableHeight}px + ${monthIndex * 11}px)"
        topMonth.addEventListener("transitionend", {
            resolve(Unit)
        })
    }
}

val monthSizes: HashMap<Int, Int> = hashMapOf(
    1 to 31,
    2 to 28,
    3 to 31,
    4 to 30,
    5 to 31,
    6 to 30,
    7 to 31,
    8 to 31,
    9 to 30,
    10 to 31,
    11 to 30,
    12 to 31,
)

val TABLES = listOf(
    Table("Template", months = List(12) { month ->
        Month(NormalDate(2021, month + 1, 1), List(monthSizes[month + 1]!!) { day -> Day(day + 1, false) })
    }),
)

fun requestTables() {
    val request = XMLHttpRequest()
    request.open("GET", "/calendar")
    request.onload = {
        request.responseJsonMessage(ListSerializer(String.serializer())).message.map {
            setChosen(it, true)
        }
        addTables(TABLES)
    }
    request.send()
}

fun setChosen(date: String, value: Boolean) {
    // TODO: 15.12.2020 add checks
    date.split("/").run {
        TABLES[0].months[get(1).toInt() - 1].days[get(0).toInt() - 1].chosen = value
    }
}

fun getChosen(date: String): Boolean {
    date.split("/").run {
        return TABLES[0].months[get(1).toInt() - 1].days[get(0).toInt() - 1].chosen
    }
}

fun requestName() {
    val name = document.getElementById("acc-name")!! as HTMLSpanElement

    val request = XMLHttpRequest()
    request.open("POST", "/payload")
    request.onload = {
        val accountName = request.responseJsonMessage(MapSerializer(String.serializer(), String.serializer()))
            .message["name"].toString()
        // TODO: 13.12.2020 check box size
        name.innerHTML = accountName
        Unit
    }
    request.send(Json.encodeToString(arrayOf("name")))
}
