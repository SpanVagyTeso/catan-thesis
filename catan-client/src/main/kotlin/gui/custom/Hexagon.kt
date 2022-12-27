package gui.custom

import com.catan.sdk.entities.FieldType.*
import com.catan.sdk.entities.Tile
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import tornadofx.attachTo
import kotlin.math.cos
import kotlin.math.sin

private const val radianStep = (2 * Math.PI) / 6

// ID -> (x, y)
val corners = hashMapOf<String, Pair<Double, Double>>()

class Hexagon(
    size: Double,
    var tile: Tile
): Group() {
    var polygon: Polygon = Polygon()
    var rolledNumberCircle: CircleText

    var offsetY: Double = 0.0
        set(amount) {
            field = amount
            morphHexagon()
        }
    var offsetX: Double = 0.0
        set(amount) {
            field = amount
            morphHexagon()
        }
    var defaultAngle: Double = Math.PI / 6
        set(amount) {
            field = amount
            morphHexagon()
        }
    var height: Double = 0.0
        set(amount) {
            field = amount
            morphHexagon()
        }

    init {
        polygon.id = tile.id
        rolledNumberCircle = CircleText(size/4, tile.rolledNumber.toString())
        rolledNumberCircle.setAllId(tile.id)

        this.height = size

        children.add(polygon)
        children.add(rolledNumberCircle)

        morphHexagon()
    }

    private fun morphHexagon() {
        polygon.fill = when (tile.type) {
            FOREST -> Color.DARKGREEN
            PASTURE -> Color.GREEN
            MOUNTAINS -> Color.ORANGE
            HILLS -> Color.LIGHTGREEN
            FIELDS -> Color.LIGHTYELLOW
            DESERT -> Color.YELLOW
            OCEAN -> Color.BLUE
        }

        polygon.points.clear()
        polygon.strokeWidth = 1.0
        polygon.stroke = Color.BLACK
        val side = height / cos(Math.PI / 6.0)
        for (i in 0 .. 5) {
            val angle = radianStep * (1.0 - i.toDouble()) + defaultAngle
            val x = cos(angle) * side - offsetX
            val y = sin(angle) * side - offsetY
            polygon.points.add(x)
            polygon.points.add(y)

            corners[tile.vertices[i]!!.id] = x to y
        }
        rolledNumberCircle.centerX(-offsetX)
        rolledNumberCircle.centerY(-offsetY)
        rolledNumberCircle.circle.fill= Color.WHITE
    }
}

fun Parent.hexagon(x: Number = 0.0, tile: Tile, op: Hexagon.() -> Unit = {}) =
    Hexagon(x.toDouble(), tile).attachTo(this, op)