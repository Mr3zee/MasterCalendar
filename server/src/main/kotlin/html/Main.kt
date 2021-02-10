package html

import kotlinx.html.*


class Main : AbstractHTMLPage(
    title = "Master Calendar",
    css = arrayOf("main", "root", "cell"),
    protected = true,
) {
    override fun body(body: BODY) {
        return body.div {
            this@Main.main(this)
            this@Main.fixed(this)
        }
    }

    private fun main(div: DIV) {
        return div.div {
            id = "main"
            div("outline") {
                id = "outline1"
            }
            div("outline") {
                id = "outline2"
            }
            div("tables") {
                div {
                    id = "wrapper"
                    div {
                        id = "main-container"
                    }
                }
            }
        }
    }

    private fun fixed(div: DIV) {
        return div.div("fixed") {
            this@Main.infoBar(this)
            this@Main.header(this)
        }
    }

    private fun infoBar(div: DIV) {
        return div.div {
            id = "info-bar"
            div("table-info-wrapper") {
                div("table-info") {
                    div("ti-name") {
                        span { +"Table Name" }
                    }
                    div("ti-img") {
                        img {
                            src = "/images/default_table_img.png"
                            alt = "Here the image should be"
                        }
                    }
                    div("ti-main") {
                        div("ti-main-attr") {
                            span("ti-main-attr-key") { +"Owner:" }
                            span("ti-main-attr-value") { +"Me" }
                        }
                        div("ti-main-attr") {
                            span("ti-main-attr-key") { +"Created:" }
                            span("ti-main-attr-value") { +"21.11.20, 00:16" }
                        }
                        div("ti-main-attr") {
                            span("ti-main-attr-key") { +"Last Modified:" }
                            span("ti-main-attr-value") { +"21.11.20, 00:17" }
                        }
                        div("ti-main-attr") {
                            span("ti-main-attr-key") { +"Public:" }
                            span("ti-main-attr-value") { +"False" }
                        }
                    }
                }
            }
            div("show-button") {
                input {
                    type = InputType.button
                    id = "hide-show-button"
                    value = "Hide Sidebar"
                }
            }
        }
    }

    private fun header(div: DIV) {
        return div.div("header-wrapper") {
            div("header") {
                div("calendar-name") {
                    p {
                        id = "calendar-name-text"
                        +"Master Calendar"
                    }
                }
                div("info") {
                    div("info-element") {
                        id = "list"
                        span {
                            +"My List"
                        }
                        div("calendar-list") {
                            span { +"New Calendar" }
                            span { +"Calendar 1" }
                            span { +"Calendar 2" }
                            span { +"Calendar 3" }
                            span { +"+" }
                        }
                        div("list-border") { }
                    }
                    div("info-element") {
                        id = "settings"
                        a {
                            href = "/settings"
                            +"Settings"
                        }
                    }
                    div("info-element") {
                        span {
                            id = "acc-name"
                        }
                    }
                }
            }
        }
    }
}
