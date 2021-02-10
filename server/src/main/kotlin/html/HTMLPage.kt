package html

import kotlinx.html.HTML

interface HTMLPage {
    fun html(html: HTML, ssid: String? = null): Array<Unit>

    fun isProtected(): Boolean
}