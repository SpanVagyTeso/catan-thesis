package gui.views

import com.catan.sdk.toJson
import tornadofx.*

class LobbyView : BaseView() {


    override fun refresh() {
        println("Lobby refreshed")
        root.children.clear()
        root.gridpane {
            with(controller.currentLobby) {
                row {
                    label {
                        text = lobbyName
                    }
                }
                users.forEach { user ->
                    row {
                        label {
                            text = (if (user == ownerUser) {
                                "\uD83D\uDC51 "
                            } else {
                                ""
                            }) + user
                        }
                    }
                }
                row {
                    if (controller.username == ownerUser) {
                        button {
                            text = "Start"
                            action {
                                startLobby()
                            }
                        }
                    }
                    button {
                        text = "Leave"
                        action {
                            leaveLobby()
                        }
                    }
                }
            }
        }
    }

    private fun startLobby() {
        controller.startLobby()
    }

    private fun leaveLobby() {
        controller.leaveLobby()
        runLater {
            controller.currentView.replaceWith<LobbySelectionView>()
        }
    }

    override val root = flowpane {}
}
