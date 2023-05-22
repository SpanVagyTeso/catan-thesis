package gui

import com.catan.sdk.entities.Edge
import com.catan.sdk.entities.FieldType
import com.catan.sdk.entities.Tile
import com.catan.sdk.entities.Vertex
import controller.ViewController
import gui.custom.hexagon
import gui.views.LoginView
import gui.views.RegisterView
import javafx.application.Platform
import javafx.geometry.Pos
import tornadofx.*

class MainView : View(title = "Catan") {

    private val controller: ViewController by inject()

    init {
        controller.currentView = this
    }

    override val root = borderpane {
        println("main root")
        setPrefSize(800.0, 600.0)
        center = vbox(alignment = Pos.CENTER) {
            label {
                text = "Welcome to catan"

            }
            button {
                text = "Register"
                action {
                    Platform.runLater {
                        replaceWith<RegisterView>()
                    }
                }
            }
            button {
                text = "Login"
                action {
                    Platform.runLater {
                        replaceWith<LoginView>()
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        controller.currentView = this
        currentWindow?.let {
            it.height = 768.0
            it.width = 1028.0
        }
    }
}

