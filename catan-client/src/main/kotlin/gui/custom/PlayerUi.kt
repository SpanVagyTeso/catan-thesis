package gui.custom

import com.catan.sdk.entities.ResourceType
import controller.GameController
import controller.GameState.*
import javafx.scene.layout.GridPane
import tornadofx.*

class PlayerUi(
    private val gameController: GameController,
    val developmentsAction: () -> Unit,
    val buyAction: () -> Unit,
    val passAction: () -> Unit,
    val refresh: () -> Unit,
) : GridPane() {

    init {
        morph()
    }

    fun morph() {
        clear()
        row {
            label {
                text = gameController.me!!.username
            }
        }
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