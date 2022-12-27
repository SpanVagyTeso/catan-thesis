package gui.views

import controller.ViewController
import javafx.application.Platform
import tornadofx.*

class RegisterView: BaseView() {
    init {
        println("register")
    }

    override val root = vbox {
        println("register root")
        setPrefSize(400.0,200.0)

        label {
            text = "Username"
        }
        val username = textfield {
            maxWidth=200.0
        }
        label{
            text = "Password"
        }
        val password = passwordfield {
            maxWidth=200.0
        }
        button {
            text = "Register"
            action{
                controller.register(username.text, password.text)
                Platform.runLater{
                    replaceWith<LoginView>()
                }
            }
        }
    }

    override fun refresh() {}
}