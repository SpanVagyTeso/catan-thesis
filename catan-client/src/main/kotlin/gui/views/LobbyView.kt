package gui.views

import com.catan.sdk.toJson
import tornadofx.*

class LobbyView:BaseView() {


    override fun refresh() {
        println("Lobby refreshed")
        println(controller.currentLobby.toJson())
        root.children.clear()
        root.gridpane {
            with(controller.currentLobby){
                row{
                    label{
                        text = lobbyName
                    }
                }
                users.forEach { user ->
                    row {
                        label {
                            text = (if(user == ownerUser){"\uD83D\uDC51 "}else{""})+ user
                        }
                    }
                }
                row{
                    if(controller.username == ownerUser){
                        button {
                            text = "start"
                            action {
                                startLobby()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startLobby(){
        controller.startLobby()
    }

    override val root = flowpane {}
}