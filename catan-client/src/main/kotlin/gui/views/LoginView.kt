package gui.views

import controller.ViewController
import javafx.geometry.Pos
import tornadofx.*

class LoginView: BaseView() {
    override fun refresh() {}

    override val root = vbox(alignment = Pos.CENTER){
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
            text = "Press me"
            action{
                controller.login(username.text, password.text)
            }
        }
    }
}