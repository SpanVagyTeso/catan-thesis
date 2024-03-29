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
        }
        row{
            label {
                text = "Unused Developments: ${player.hiddenDevelopments}"
                paddingLeft = 2.0
            }
        }
        row {
            label {
                text = "Used Knights: ${player.activeDevelopments.count { it.developmentTypes == Knight }}"
            }
        }
        row {
            if(player.ownerOfMostKnights){
                label {
                    text = "Most Knight(+2 Point)"
                    paddingLeft = 2.0
                }
            }
            if(player.ownerOfLongestRoad){
                label {
                    text = "Longest road(+2 Point)"
                }
            }
        }
        vgrow = Priority.NEVER

    }
}
