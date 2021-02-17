package html

import kotlinx.html.*

class Register : AbstractHTMLPage(
    title = "Register",
    css = arrayOf("authorization", "root"),
    protected = false,
) {
    override fun body(body: BODY) {
        return body.div(classes = "body") {
            div(classes = "body-class") {
                id = "body-reg"
                div("main") {
                    id = "register"
                    div("container") {
                        div(classes = "header") {
                            id = "reg-head"
                            img {
                                src = "/images/logo.jpg"
                                alt = "Login"
                                height = "300"
                                width = "300"
                            }

                        }
                        form(classes = "input") {
                            id = "reg-input"
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.text
                                    id = "reg-input1"
                                    name = "name"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "reg-input1"
                                    +"Name"
                                }
                            }
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.email
                                    id = "reg-input2"
                                    name = "email"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "reg-input2"
                                    +"Email"
                                }
                            }
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.password
                                    id = "reg-input3"
                                    name = "password"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "reg-input3"
                                    +"Password"
                                }
                            }
                            p("field") {
                                input(classes = "form-input") {
                                    type = InputType.password
                                    id = "reg-input4"
                                    name = "repeat-password"
                                }
                                label(classes = "form-label inside") {
                                    htmlFor = "reg-input4"
                                    +"Repeat Password"
                                }
                            }
                            input {
                                type = InputType.button
                                value = "Register"
                                id = "button"
                            }
                        }
                        div("footer") {
                            p("footer-text") {
                                +"Already have an account? "
                                a {
                                    href = "/login"
                                    id = "reg-link"
                                    +"Log in"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}