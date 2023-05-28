package gui

import com.catan.sdk.entities.FieldType
import com.catan.sdk.entities.Tile
import com.catan.sdk.entities.Vertex
import controller.ViewController
import gui.custom.hexagon
import gui.views.LoginView
import gui.views.RegisterView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Parent
import tornadofx.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

class MainView : View(title = "Catan") {

    private val controller: ViewController by inject()

    init {
        controller.currentView = this
    }

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
        paddingAll = 15.0
        center = vbox(alignment = Pos.CENTER) {
            label {
                text = "Welcome to Catan"

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

    private fun calculateYOffSetFromHeight(height: Double) = sqrt(
            (height / cos(Math.PI / 60) * 2).pow(2.0) - (height.pow(2.0))
    )

    private fun Parent.drawHexagons(map: MutableList<MutableList<Tile>>) {
        val TILE_HEIGHT = 60.0
        val yOffSet = calculateYOffSetFromHeight(TILE_HEIGHT)
        val s = map.size
        var off = s / 2
        map.forEachIndexed { rowInd, row ->
            row.forEachIndexed { colInd, it ->
                hexagon(TILE_HEIGHT, it) {
                    offsetX = TILE_HEIGHT * 2 * colInd - TILE_HEIGHT * off
                    offsetY = yOffSet * rowInd
                }
            }
            if (rowInd >= s / 2) off--
            else off++
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

