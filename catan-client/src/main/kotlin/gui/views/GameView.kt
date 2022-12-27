package gui.views

import gui.custom.*
import javafx.event.EventHandler
import javafx.scene.effect.BoxBlur
import javafx.scene.effect.Effect
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Color.*
import tornadofx.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

private const val TILE_HEIGHT = 60.0

class GameView: BaseView() {

    val center = stackpane()
    val playField = borderpane {
        bottom = gridpane{
            row{
                button{
                    text = "Buy"
                    action{
                        buyMenu()
                    }
                }
            }
        }
        this@borderpane.center = this@GameView.center
        left = gridpane {
            //other player
        }
        right = gridpane{
            //other player
        }
        top = gridpane{
            //other player
        }

        onKeyPressed = EventHandler {
            println("Key pressed")
            if(it.code == KeyCode.ESCAPE){
                state = 0
                closeBuyMenu()
                refresh()
            }
        }

    }
    var state: Int = 0
    set(value){
        field = value
        refresh()
    }

    val blur: BoxBlur = BoxBlur()

    val popUp = stackpane{}

    override fun refresh() {
        val yOffSet = calculateYOffSetFromHeight(TILE_HEIGHT)
        center.clear()
        center.apply {
            group{
                val s = controller.map().size
                var off = s/2
                controller.map().forEachIndexed { rowInd, row ->
                    println("row")
                    row.forEachIndexed { colInd, it ->
                        hexagon(TILE_HEIGHT, it){
                            offsetX = TILE_HEIGHT * 2 * colInd - TILE_HEIGHT * off
                            offsetY = yOffSet * rowInd
                        }
                    }
                    if(rowInd >= s/2) off--
                    else off++
                }
                if(state == 4){
                    //Available roads
                    controller.gameController.map.edges.forEach {
                        line {
                            val endpoint = it.endPoints
                            startX = corners[endpoint.first.id]!!.first
                            startY = corners[endpoint.first.id]!!.second
                            endX = corners[endpoint.second.id]!!.first
                            endY = corners[endpoint.second.id]!!.second
                            strokeWidth = 3.0
                            id = it.id
                            stroke = RED
                        }
                    }
                }
                if(state == 5 || state == 6){
                    //Available corner locations
                    corners.forEach { (k, v) ->
//                        println("$k -> ${v.first} ${v.second}")
                        circleText(TILE_HEIGHT/5, k){
                            setAllId(k)
                            centerX(v.first)
                            centerY(v.second)
                            circle.fill = WHITE
                        }
                    }
                }

                onMouseClicked = EventHandler {
                    if(it.button == MouseButton.PRIMARY){
                        val id = it.pickResult.intersectedNode.id
                        println("Clicked id: $id $state")
                        val t = controller.gameController.map.tile(id)
                        if(t.size == 1){
                            val tt = t[0]
//                            tt.vertices.forEach {
//                                println(it)
//                            }
                        }
                        if(id.startsWith("E")){
                            println("EDGE clicked")
                            if(state == 4) {
                                state = 0
                                controller.buyRoad(id)
                            }
                        }
                        if(id.startsWith("V")){
                            if(state == 5){
                                state = 0
                                controller.buyVillage(id)
                            }
                            else if( state == 6){
                                state =0
                                controller.buyCity(id)
                            }
                        }
                    }
                }
            }
        }
    }

    override val root = stackpane {
        add(playField)
    }

    private fun calculateYOffSetFromHeight(height: Double): Double {
        return sqrt(
            (height / cos(Math.PI / 60) * 2).pow(2.0) - (height.pow(2.0))
        )
    }

    fun buyMenu(){
        state = 1
        popUp.apply{
            rectangle {
                width = root.width/2
                height = root.height/2
                fill = WHITE
            }
            group{
                gridpane{
                    hgap = 10.0
                    row{
                        label {
                            text="Resources"
                        }
                    }
                    row{
                        label {
                            text="Road"
                        }
                        label {
                            text="Village"
                        }
                        label {
                            text="City"
                        }
                        label {
                            text="Upgrade"
                        }
                    }
                    row{
                        rectangle {
                            width = 10.0
                            height = 10.0
                        }
                        village {
                            size=25.0
                        }
                        city{
                            size=25.0
                        }
                        rectangle {
                            width = 10.0
                            height = 10.0
                            fill = YELLOW
                        }
                    }
                    row{
                        button {
                            text="Buy"
                            action {
                                closeBuyMenu()
                                buyRoad()
                            }
                        }
                        button {
                            text="Buy"
                            action {
                                closeBuyMenu()
                                buyVillage()
                            }
                        }
                        button {
                            text="Buy"
                            action {
                                closeBuyMenu()
                                buyCity()
                            }
                        }
                        button {
                            text="Buy"
                            action {
                                closeBuyMenu()
                                buyUpgrade()
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun closeBuyMenu(){
        playField.effect = null
        root.clear()
        root.add(playField)
        blur.height = 0.0
        blur.width = 0.0
    }

    fun buyRoad(){
        state = 4
    }

    fun buyVillage(){
        state = 5
    }

    fun buyCity(){
        state = 6
    }

    fun buyUpgrade(){
        controller.buyUpgrade()
        state = 0
    }

}