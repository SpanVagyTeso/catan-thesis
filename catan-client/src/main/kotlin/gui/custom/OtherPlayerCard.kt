package gui.custom

import com.catan.sdk.entities.DevelopmentTypes.Knight
import com.catan.sdk.entities.Player
import gui.views.toJavaColor
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import tornadofx.*

class OtherPlayerCard(val player: Player) : GridPane() {

    init {
        morph()
        style = "-fx-border-color: black"
    }

    fun morph() {
        clear()
        row {
            gridpane {
                row {
                    circle {
                        radius = 5.0
                        fill = player.playerColor.toJavaColor()
                    }
                    label {
                        text = player.username
                    }
                }
            }
        }
        row {
            vgap = 2.0
            label {
                text = "Resources: ${player.hiddenResources}"
            }
            label {
                text = "Unused Development: ${player.hiddenDevelopments}"
            }
            label {
                text = "Knights: ${player.activeDevelopments.count { it.developmentTypes == Knight }}"
            }
        }
        vgrow = Priority.NEVER

    }
}
