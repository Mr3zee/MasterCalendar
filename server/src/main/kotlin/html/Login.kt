package html

import kotlinx.html.*

// TODO: 08.12.2020 add captions

// TODO: 10.12.2020 add remember me button

class Login : AbstractHTMLPage(
    title = "Login",
    css = arrayOf("authorization", "root"),
    protected = false,
) {
    override fun body(body: BODY) {
        return body.div(classes = "body") {
            div(classes = "body-class") {
                id = "body-login"
                div("main") {
                    id = "login"
                    div("container") {
                        div(classes = "header") {
                            id = "log-head"
                            img {
                                src = "/images/logo.jpg"
                                alt = "Login"
                            }
                        }
                        div(classes = "input") {
                            id = "log-input"
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.email
                                    id = "log-input1"
                                    name = "email"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "input1"
                                    +"Email"
                                }
                            }
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.password
                                    id = "log-input2"
                                    name = "password"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "input2"
                                    +"Password"
                                }
                            }
                            input {
                                type = InputType.button
                                value = "Log in"
                                id = "button"
                            }
                        }
                        div("footer") {
                            p("footer-text") {
                                +"Do not have an account? "
                                a {
                                    href = "/register"
                                    id = "reg-link"
                                    +"Register"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
