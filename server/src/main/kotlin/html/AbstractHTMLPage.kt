package html

import com.fasterxml.jackson.core.util.RequestPayload
import kotlinx.html.*
import server.addAntiCSRFToken
import server.getUserAttrs
import java.lang.IllegalArgumentException

abstract class AbstractHTMLPage(
    private val title: String,
    private val css: Array<String>,
    private val protected: Boolean,
) : HTMLPage {

    override fun isProtected(): Boolean {
        return protected
    }

    override fun html(html: HTML, ssid: String?): Array<Unit> {
        return arrayOf(head(html), html.body {
            addAntiCSRFToken(this, ssid)
            body(this)
        })
    }

    protected abstract fun body(body: BODY)

    private fun head(html: HTML) {
        return html.head {
            meta {
                charset = "UTF-8"
            }
            title { +this@AbstractHTMLPage.title }
            link {
                rel = "icon"
                href = "/favicon.ico"
            }
            this@AbstractHTMLPage.css.map {
                link {
                    rel = "stylesheet"
                    href = "/css/$it.css"
                }
            }
            link {
                rel = "stylesheet"
                href = "https://fonts.googleapis.com/css2?family=Montserrat:wght@500&display=swap"
            }

            script { src = "/client.js" }
        }
    }
}