import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.response.*

import html.*

import io.ktor.features.*
import server.*


// TODO: 08.12.2020
//  customize 404 page
//  customize welcome page
//  customize forbidden page

// TODO: 11.12.2020
//  make cookie usage alert

// TODO: 11.12.2020
//  add ssl

// TODO: 12.12.2020 email confirmation

val pageMap: HashMap<String, HTMLPage> = hashMapOf(
    "/" to Main(),
    "/login" to Login(),
    "/register" to Register(),
    "/settings" to Settings(),
    "/404" to NotFound(),
)

fun Application.main() {

    // TODO: 15.12.2020 add proper shutdown on error
    initServices()

    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respondRedirect("/404", permanent = true)
        }
    }

    routing {

        pageMap.map { pagePair ->
            get(pagePair.key) {
                val page = pagePair.value
                if (page.isProtected()) {
                    call.showProtectedPage(page)
                } else {
                    call.showAuthPage(page)
                }
            }
        }

        post("/auth") {
            call.processRequest()
        }

        post("/payload") {
            call.respondPayload()
        }

        post("/logout") {
            call.deleteSession()
        }

        get("/validateEmail") {
            println("Got validation")
            call.redirectPage("/")
        }

        post("/calendar") {
            call.processPOSTCalendar()
        }

        get("/calendar") {
            call.getCalendar()
        }

        static("/") {
            resources("/")
        }
    }
}
