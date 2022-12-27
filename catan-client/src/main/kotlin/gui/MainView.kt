package gui

import controller.ViewController
import gui.custom.city
import gui.custom.village
import gui.views.LoginView
import gui.views.RegisterView
import javafx.application.Platform
import javafx.geometry.Pos
import tornadofx.*

class MainView : View(title="Catan") {

    private val controller: ViewController by inject()
    init{
        controller.currentView = this
    }
    override val root = borderpane {
        println("main root")
        setPrefSize(800.0, 600.0)
        center = vbox(alignment = Pos.CENTER){
            label {
                text = "Welcome to catan"

            }
            button {
                text = "Register"
                action{
                    Platform.runLater {
                        replaceWith<RegisterView>()
                    }
                }
            }
            button {
                text = "Login"
                action{
                    Platform.runLater {
                        replaceWith<LoginView>()
                    }
                }
            }

        }
        left = city {
            size = 30.0
        }

    }

    override fun onDock() {
        super.onDock()
        controller.currentView = this
    }
}

