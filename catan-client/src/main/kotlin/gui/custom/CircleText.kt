package gui.custom

import javafx.scene.Parent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextBoundsType.VISUAL
import tornadofx.add
import tornadofx.attachTo

class CircleText(
    circleRadius: Double,
    circleText: String
) : StackPane() {

    val circle: Circle = Circle(circleRadius)
    val text: Text = Text(circleText)

    fun setAllId(id: String) {
        circle.id = id
        text.id = id
        this.id = id
    }

    init {
        text.boundsType = VISUAL
        add(circle)
        add(text)

    }

    fun centerX(x: Double) {
        layoutX = x - boundsInLocal.maxX
    }

    fun centerY(y: Double) {
        layoutY = y - boundsInLocal.maxY
    }

}

fun Parent.circleText(radius: Double, text: String, op: CircleText.() -> Unit = {}) =
    CircleText(radius, text).attachTo(this, op)
