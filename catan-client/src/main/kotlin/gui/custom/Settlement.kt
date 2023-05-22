package gui.custom

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import tornadofx.*

class Settlement : StackPane() {

    val walls: Rectangle
    val roof: Polygon
    var oId = ""
        set(value) {
            field = value
            morph()
        }
    var isHighlighted = false
        set(value) {
            field = value
            morph()
        }

    var size: Double = 15.0
        set(value) {
            field = value
            morph()
        }
    var color: Color = Color.BLACK
        set(value) {
            field = value
            morph()
        }

    init {
        walls = Rectangle(size, size)
        roof = Polygon()
        morph()
    }

    private fun morph() {
        clear()
        if (isHighlighted) {
            circle {
                fill = Color.BLACK
                radius = size * 1.3
                id = oId
            }
            circle {
                fill = Color.LIGHTGRAY
                radius = size * 1.25
                id = oId
            }
        }
        gridpane {
            walls.height = size
            walls.width = size
            roof.points.clear()
            roof.points.addAll(
                listOf(
                    0.0,
                    0.0,
                    size,
                    0.0,
                    size / 2,
                    -size * 0.65,
                )
            )
            roof.fill = color
            walls.fill = color
            roof.id = oId
            walls.id = oId

            row {
                add(roof)
            }
            row {
                add(walls)
            }
            alignment = Pos.BASELINE_CENTER
            id = oId
        }
    }

}

fun Parent.settlement(op: Settlement.() -> Unit = {}) =
    Settlement().attachTo(this, op)
