package html

import kotlinx.html.*

class Settings : AbstractHTMLPage(
    title = "Settings",
    css = arrayOf("settings", "root"),
    protected = true,
) {
    override fun body(body: BODY) {
        return body.div {
            div("main") {
                div("block") {
                    id = "header"
                    div("name") {
                        span { +"Settings" }
                    }
                    div("go-back") {
                        a {
                            href = "/"
                            +"Return to the Main Page"
                        }
                        input {
                            id = "logout"
                            type = InputType.button
                            value = "Log Out"
                        }
                    }
                }
                makeProperty(
                    main = this,
                    propertyType = "email",
                    headerValue = "Email",
                    inputs = arrayOf(
                        Input(
                            id = "email",
                            label = "Enter new email address",
                            inputType = InputType.email,
                            placeholder = "Email",
                            caption = "*After submitting you will receive an email with conformation"
                        )
                    ),
                    buttonValue = "Submit"
                )
                makeProperty(
                    main = this,
                    propertyType = "name",
                    headerValue = "Account Name",
                    inputs = arrayOf(
                        Input(
                            id = "name",
                            label = "Enter new account name",
                            inputType = InputType.text,
                            placeholder = "Account Name",
                            caption = "*Other users can find your public calendars by your account's name"
                        )
                    ),
                    buttonValue = "Submit"
                )
                makeProperty(
                    main = this,
                    propertyType = "password",
                    headerValue = "Password",
                    inputs = arrayOf(
                        Input(
                            id = "password",
                            label = "Enter new password",
                            inputType = InputType.password,
                            placeholder = "Password",
                            caption = "*Password should be at least 12 symbols and max of 24, " +
                                    "containing lower and upper case letters, digits and special characters: @\\$!%*?&"
                        ),
                        Input(
                            id = "repeat-password",
                            label = "Repeat your password",
                            inputType = InputType.password,
                            placeholder = "Repeat Password",
                            caption = "*Enter the same password again"
                        )
                    ),
                    buttonValue = "Submit"
                )
                makeProperty(
                    main = this,
                    propertyType = "delete",
                    headerValue = "Delete Account",
                    inputs = arrayOf(
                        Input(
                            id = "delete",
                            label = "Enter your account name to confirm deletion",
                            inputType = InputType.text,
                            placeholder = "Account Name",
                            caption = "WARNING! This action cannot be undone! Your account will be deleted completely!"
                        ),
                    ),
                    buttonValue = "Continue",
                )
            }
        }
    }
}

fun makeProperty(
    main: DIV,
    propertyType: String,
    headerValue: String,
    inputs: Array<Input>,
    buttonValue: String,
    body: (DIV, String, Array<Input>) -> Unit = { mainInner, propertyTypeInner, inputsInner ->
        defaultBody(mainInner, propertyTypeInner, inputsInner)
    },
) {
    return main.div("block") {
        id = "prop-$propertyType"
        div("prop-name") {
            div("name-container") {
                id = "name-container-$propertyType"
                span(classes = "header-text") {
                    id = "header-text-$propertyType"
                    +headerValue
                }
            }
        }
        body(main, propertyType, inputs)
        div("submit-change") {
            input(classes = "submit-button") {
                type = InputType.button
                value = buttonValue
                id = "${propertyType}-btn"
            }
        }
    }
}

fun defaultBody(main: DIV, propertyType: String, inputs: Array<Input>) {
    return main.div (classes = "change-block") {
        id = "change-block-$propertyType"
        this.div(classes = "change-page") {
            id = "page-$propertyType"
            div("field-container") {
                makeInputs(this, inputs)
            }
        }
    }
}

fun makeInputs(div: DIV, inputs: Array<Input>): List<Unit> {
    return inputs.map {
        div.div("field") {
            id = "f-${it.id}"
            label { +it.label }
            input {
                type = it.inputType
                id = "input-${it.id}"
                placeholder = it.placeholder
            }
            span("caption") { +it.caption }
        }
    }
}

data class Input(
    val id: String,
    val label: String,
    val inputType: InputType,
    val placeholder: String,
    val caption: String,
)