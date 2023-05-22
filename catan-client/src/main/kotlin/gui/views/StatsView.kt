package gui.views

import javafx.geometry.Pos
import tornadofx.*

class StatsView : BaseView() {
    val statsPane = vbox (alignment = Pos.CENTER){   }
    override fun refresh() {
        statsPane.clear()
        statsPane.apply {
            if (controller.stats != null) {
                with(controller.stats!!) {
                    if (gamesPlayed == -1) {
                        label {
                            text = "Username not found!"
                        }
                    } else {
                        label {
                            text = "Username: ${this@with.username}"
                        }
                        label {
                            text = "Games played: $gamesPlayed"
                        }
                        label {
                            text = "Games won: $gamesWon"
                        }
                    }
                }
            }
        }
    }

    override val root = vbox(alignment = Pos.CENTER) {
        setPrefSize(400.0, 200.0)
        label {
            text = "Username"
        }
        val username = textfield {
            maxWidth = 200.0
        }

        button {
            text = "Get Stats"
            action {
                controller.getStats(username.text)
            }
        }
        add(statsPane)
        button {
            text = "Back to lobbies"
            action {
                runLater {
                    controller.currentView.replaceWith<LobbySelectionView>()
                    controller.refreshCurrentView()
                }
            }
        }

    }
}
