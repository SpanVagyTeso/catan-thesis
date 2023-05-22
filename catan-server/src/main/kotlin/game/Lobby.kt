package game

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.game.fromserver.StartupDto
import com.catan.sdk.dto.lobby.LobbyDto
import com.catan.sdk.toJson
import error.LobbyFull
import error.LobbyIsStarted
import kotlinx.coroutines.runBlocking
import service.GameService
import service.SessionService

class Lobby(
    val lobbyId: String,
    private val size: Int,
    private var owner: String,
    private val lobbyName: String,
    private val sessionService: SessionService,
    private val gameService: GameService
) {
    var isStarted = false

    private val players = mutableListOf<String>()
    lateinit var game: Game

    init {
        players.add(owner)
    }

    fun join(userName: String) {
        if (isStarted) throw LobbyIsStarted()
        if (players.size < size) {
            players.add(userName)
        } else {
            throw LobbyFull()
        }
        refreshLobby()
    }

    private fun refreshLobby() {
        sendPlayers(this.toDto())
    }

    private fun sendPlayers(dto: DtoType) {
        players.forEach {
            runBlocking {
                sessionService.getSessionInfoFromUsername(it).socket.sendMessage(
                    dto.toJson()
                )
            }
        }
    }

    fun isOwner(username: String) = owner == username

    fun start() {
        isStarted = true
        game = gameService.startGame(
            players
        )
        sendPlayers(
            StartupDto(
                game.currentPlayer.username,
                game.map.toDto(),
                game.players.map {
                    it.toDto()
                },
                game.map.getMaritimeLocations()
            )
        )
    }

    fun leave(username: String) {
        players.removeIf {
            it == username
        }
        refreshLobby()
    }

    fun isEmpty() = players.isEmpty()

    fun toDto(): LobbyDto {
        return LobbyDto(
            owner,
            players.toTypedArray(),
            size,
            lobbyName,
            lobbyId
        )
    }
}
