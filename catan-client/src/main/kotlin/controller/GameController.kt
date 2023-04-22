package controller

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.STARTUP
import com.catan.sdk.dto.game.out.StartupDto
import com.catan.sdk.entities.*
import com.catan.sdk.entities.Map
import com.catan.sdk.toDto
import gui.views.GameView
import javafx.application.Platform

class GameController(
    private val viewController: ViewController
) {
    val map = Map()

    private fun startup(dto: StartupDto) {
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
        val player = Player("alma")
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
}
