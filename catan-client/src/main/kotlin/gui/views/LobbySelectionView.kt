package gui.views

import com.catan.sdk.dto.lobby.LobbyDto
import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import tornadofx.*


class LobbySelectionView : BaseView() {
    private val lobbyFlow = flowpane {}
    private var selectedId: String = ""


    override fun refresh() {
        println("Lobby Selection refreshed")
        val lobbies = controller.lobbies
        lobbyFlow.clear()
        lobbyFlow.apply {
            hgap = 5.0
            vgap = 5.0
            lobbies.forEach {
                this.children.add(it.toGrid())
            }
        }
    }

    private fun refreshLobbies() {
        controller.sendGetLobbies()
    }

    private fun createLobby() {
        controller.createLobby()
    }

    private fun joinLobby() {
        if (selectedId == "") return
        controller.joinLobby(selectedId)
    }

    override val root = borderpane {
        center = scrollpane {
            refresh()
            add(lobbyFlow)
        }
        right = vbox {
            button {
                text = "Refresh"
                action {
                    refreshLobbies()
                }
            }
            button {
                text = "Create"
                action {
                    createLobby()
                }
            }
            button {
                text = "Join"
                action {
                    joinLobby()
                }
            }
        }
    }

    private fun LobbyDto.toGrid(): GridPane {
        return gridpane {
            row {
                hgap = 10.0
                label {
                    text = this@toGrid.lobbyName
                    style {
                        fontSize = 14.px
                    }
                }
                label {
                    text = "Size: " + this@toGrid.maxSize
                }

            }

            this@toGrid.users.forEach {
                row {
                    label {
                        text = (if (it == this@toGrid.ownerUser) {
                            "\uD83D\uDC51 "
                        } else {
                            ""
                        }) + it
                    }
                    style {
                        borderColor += box(
                            top = Color.TRANSPARENT,
                            right = Color.TRANSPARENT,
                            left = Color.TRANSPARENT,
                            bottom = Color.BLACK
                        )
                    }
                }
            }
            style {
                borderColor += box(
                    top = Color.BLACK,
                    right = Color.BLACK,
                    left = Color.BLACK,
                    bottom = Color.BLACK
                )
                padding = box(
                    top = 10.px,
                    right = 10.px,
                    left = 10.px,
                    bottom = 10.px
                )
                if (this@toGrid.sessionId == selectedId) {
                    backgroundColor += Color.LIGHTBLUE
                }
            }
            id = this@toGrid.sessionId
            onMouseClicked = EventHandler {
                if (it.button == MouseButton.PRIMARY) {
                    selectedId = if (selectedId == this@toGrid.sessionId) {
                        ""
                    } else {
                        this@toGrid.sessionId!!
                    }
                    refresh()
                }
            }
        }
    }
}
