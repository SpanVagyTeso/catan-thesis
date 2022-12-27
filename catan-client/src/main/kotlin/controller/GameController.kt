package controller

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.STARTUP
import com.catan.sdk.dto.game.out.StartupDto
import com.catan.sdk.entities.Map
import com.catan.sdk.entities.MapDto
import com.catan.sdk.toDto
import gui.views.GameView
import javafx.application.Platform

class GameController(
    private val viewController: ViewController
) {
    val map = Map()

    private fun startup(dto: StartupDto){
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        Platform.runLater{
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun handle(message: String){
        with(message.toDto<Game>()){
            when(gameType){
                STARTUP->{
                    startup(message.toDto())
                }
            }
        }
    }
}