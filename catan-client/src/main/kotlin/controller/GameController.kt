package controller

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.STARTUP
import com.catan.sdk.dto.game.out.StartupDto
import com.catan.sdk.entities.*
import com.catan.sdk.entities.Map
import com.catan.sdk.entities.PlayerColor.RED
import com.catan.sdk.toDto
import gui.views.GameView
import javafx.application.Platform

class GameController(
    private val viewController: ViewController
) {
    val map = Map()
    val players = mutableMapOf<String, Player>()


    private fun startup(dto: StartupDto) {
        dto.players.forEach {
            players[it] = Player(it, RED)
        }
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        Platform.runLater {
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun test() {
        map.generateTiles()
        map.attachAllTiles()
        var counter = 0
        val player = Player(viewController.username, RED)
        players[viewController.username] = player
        map.tiles.forEach {
            it.rolledNumber = counter
            counter++
            it.type = FieldType.values()[counter % FieldType.values().size]
            it.vertices.find {
                it?.id == "V0"
            }?.apply {
                owner = player
                buildingType = BuildType.VILLAGE
                edges[0].owner = player
            }
            it.vertices.find {
                it?.id == "V1"
            }?.apply {
                owner = player
                buildingType = BuildType.CITY
                edges[0].owner = player

            }
        }
        map.edges.forEach {
            if (it.id == "E30") {
                println("alamfa")
                it.owner = player
            }
        }
    }

    fun handle(message: String) {
        with(message.toDto<Game>()) {
            when (gameType) {
                STARTUP -> {
                    startup(message.toDto())
                }
            }
        }
    }

    fun getGoodCorners() = map.getBuyableVertexes(players[viewController.username]!!)

}
