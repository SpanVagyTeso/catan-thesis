package gui.custom

import javafx.scene.Parent
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import tornadofx.add
import tornadofx.attachTo
import tornadofx.clear
import tornadofx.row

class Village :GridPane(){

    val walls: Rectangle
    val roof: Polygon
    var size: Double = 10.0
        set(value){
            field = value
            morph()
        }
    init {
        walls = Rectangle(size, size)
        roof = Polygon()
        morph()
    }

    private fun morph(){
        clear()
        walls.height = size
        walls.width = size
        roof.points.clear()
        roof.points.addAll(
            listOf(
                0.0,
                0.0,
                size,
                0.0,
                size/2,
                -size*0.65,
            )
        )

        roof.fill = Color.RED
        row{
            add(roof)
        }
        row {
            add(walls)
        }
    }

}

fun Parent.village(op: Village.() -> Unit = {})=
    Village(). attachTo(this, op)
