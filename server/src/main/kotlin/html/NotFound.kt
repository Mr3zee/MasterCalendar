package html

import kotlinx.html.*

class NotFound: AbstractHTMLPage(
    title = "Not Found",
    css = arrayOf("notFound"),
    protected = false,
) {
    override fun body(body: BODY) {
        return body.div {
            + "sry but page not found"
        }
    }
}