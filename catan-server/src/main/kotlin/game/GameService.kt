package game

import com.catan.sdk.dto.game.fromclient.FromClient
import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.*
import com.catan.sdk.entities.Player
import com.catan.sdk.entities.PlayerColor
import com.catan.sdk.toDto
import database.DatabaseService
import game.GameState.Ended
import game.GameState.Won
import session.SessionService

class GameService(
        private val sessionService: SessionService,
        private val databaseService: DatabaseService
) {
    private val allGames = mutableListOf<Game>()

    fun handleIncomingMessage(message: String) {
        with(message.toDto<FromClient>()) {
            val userName = sessionService.getUserFromSessionId(this.sessionId)
                ?: return
            val game = allGames.find {
                it.players.find {
                    it.username == userName
                } != null
            } ?: return
            handlePayload(game, userName, message)
        }
    }

    fun socketClosed(userName: String) {
        allGames.forEach {
            it.playerLeft(userName)
            if (it.gameState == Ended) {
                allGames.remove(it)
            }
        }
    }

    private fun handlePayload(game: Game, userName: String, message: String) {
        if (game.currentPlayer.username != userName) {
            if (message.toDto<FromClient>().payloadType != AcceptTrade) return
            game.acceptOffer(message.toDto(), userName)
        }
        println("Payload Type: ${message.toDto<FromClient>().payloadType}")
        when (message.toDto<FromClient>().payloadType) {
            Monopoly -> game.useMonopoly(message.toDto())
            YearOfPlenty -> game.useYearOfPlenty(message.toDto())
            Steal -> game.steal(message.toDto())
            Buy -> game.spendResources(message.toDto())
            Pass -> game.nextPlayer()
            PlaceBeginning -> game.placeAtBeginning(message.toDto())
            TwoRoad -> game.useTwoRoad(message.toDto())
            Trade -> game.maritimeTrade(message.toDto())
            PlayerTrade -> game.handleTradeOffer(message.toDto())
            AcceptTrade -> {
                println("Invalid State, current player cannot accept trades")
            }
        }
        game.calculatePoints()
        if (game.gameState == Won) {
            allGames.remove(game)
            game.afterGameStatistics()
        } else if (game.gameState == Ended) {
            allGames.remove(game)
        }
    }

    fun startGame(players: List<String>): Game {
        val colors = mutableListOf(*PlayerColor.values())
        val newGame = Game(
            players.map {
                Player(it, colors.removeFirst())
            },
            sessionService,
            databaseService
        )
        allGames.add(newGame)

        return newGame
    }
}
