package gui.custom

import com.catan.sdk.entities.ResourceType
import controller.GameController
import controller.GameState.*
import gui.views.toJavaColor
import javafx.scene.layout.GridPane
import tornadofx.*

class PlayerUi(
    private val gameController: GameController,
    val developmentsAction: () -> Unit,
    val buyAction: () -> Unit,
    val passAction: () -> Unit,
    val refresh: () -> Unit,
    val maritimeTrade: () -> Unit,
    val playerTrade: () -> Unit
) : GridPane() {

    init {
        morph()
    }

    fun morph() {
        clear()
        row {
            hbox{
                hgap = 5.0
                circle {
                    radius = 3.0
                    fill = gameController.me!!.playerColor.toJavaColor()
                }
                label {
                    text = gameController.me!!.username
                }
                if(gameController.me!!.ownerOfMostKnights){
                    label {
                        text = "Owner of most Knight(+2 Point)"
                    }
                }
                if(gameController.me!!.ownerOfLongestRoad){
                    label {
                        text = "Owner of longest road(+2 Point)"
                    }
                }
            }
        }
        row {
            gridpane{
                row {
                    button {
                        text = "Buy"
                        isVisible = gameController.state == Normal
                        action {
                            buyAction()
                        }
                    }
                    button {
                        text = "Developments"
                        isVisible = gameController.state == Normal
                        action {
                            developmentsAction()
                        }
                    }
                    button {
                        text = "Maritime trade"
                        isVisible = gameController.state == Normal
                        action {
                            maritimeTrade()
                        }
                    }
                    button {
                        text = "Player trade"
                        isVisible = !gameController.atBeginning()
                        action {
                            playerTrade()
                        }
                    }
                    button {
                        text = "Pass turn"
                        isVisible = gameController.state == Normal
                        action {
                            passAction()
                        }
                    }
                    button {
                        text = "Back"
                        isVisible = gameController.state in setOf(UseKnight, UseRoads)
                        action {
                            gameController.state = Normal
                            refresh()
                        }
                    }
                }
                row {
                    ResourceType.values().forEach {
                        label {
                            text = "$it: ${gameController.me!!.resources[it]} "
                        }
                    }

                }
            }
        }
    }
}
