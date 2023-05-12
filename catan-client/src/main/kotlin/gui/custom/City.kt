package gui.custom

import javafx.scene.Parent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import tornadofx.*

class City : GridPane() {
    val walls: Rectangle
    val lhsRoof: Polygon
    val rhsRoof: Rectangle

    var color = Color.RED
        set(value) {
            field = value
            morph()
        }
    var size: Double = 15.0
        set(value) {
            field = value
            morph()
        }

    init {
        walls = Rectangle()
        lhsRoof = Polygon()
        rhsRoof = Rectangle()
        morph()
    }

    fun morph() {
        clear()
        walls.height = size
        walls.width = size * 2

        lhsRoof.points.clear()
        lhsRoof.points.clear()
        lhsRoof.points.addAll(
            listOf(
                0.0,
                0.0,
                size,
                0.0,
                size / 2,
                -size * 0.65,
            )
        )
        lhsRoof.fill = color
        walls.fill = color
        rhsRoof.fill = color

        rhsRoof.width = size / 3
        rhsRoof.height = size * 0.65
        row {
            hbox {
                add(lhsRoof)
                add(rhsRoof)
                spacing = size / 4
            }
        }
        row {
            add(walls)
        }
    }
}

fun Parent.city(op: City.() -> Unit = {}) =
    City().attachTo(this, op)
