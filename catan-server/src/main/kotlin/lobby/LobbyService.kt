package lobby

import com.catan.sdk.dto.lobby.*
import com.catan.sdk.toDto
import com.catan.sdk.toJson
import error.InvalidLobbyId
import error.InvalidSessionId
import error.NotOwner
import game.GameService
import session.SessionService
import socket.SocketConnection
import kotlin.random.Random

class LobbyService(
        private val sessionService: SessionService,
        private val gameService: GameService
) {
    //LobbyId to Lobby
    val lobbies = mutableMapOf<String, Lobby>()

    fun closedSocket(username: String){
        lobbies.forEach {(id, lobby) ->
            lobby.leave(username)
            if(lobby.isEmpty()) {
                lobbies.remove(id)
            }
        }
    }

    suspend fun handleCommand(message: String, socket: SocketConnection) {
        val dto: LobbyBase = message.toDto()
        if (!sessionService.validateSessionId(dto.sessionId!!)) {
            throw InvalidSessionId()
        }
        when (dto.commandType) {
            CREATE -> {
                val response = createLobby(message)
                socket.sendMessage(response.toJson())
            }

            JOIN -> {
                val response = joinLobby(message)
                socket.sendMessage(response.toJson())
            }

            REFRESH, GET_AVAILABLE_LOBBIES -> {
                val response = getAvailableLobbies()
                socket.sendMessage(response.toJson())
            }

            START -> {
                startLobby(message)
            }

            LEAVE -> {
                leaveLobby(message)
            }

            else -> {
                LobbyBase("true", "")
            }
        }
    }

    private fun leaveLobby(msg: String) {
        with(msg.toDto<LeaveLobbyDto>()) {
            val lobby = lobbies[lobbyId]!!
            lobby.leave(sessionService.getUserFromSessionId(this.sessionId!!)!!)
            if(lobby.isEmpty()) {
                lobbies.remove(lobbyId)
            }
        }
    }

    private fun startLobby(msg: String) {
        with(msg.toDto<StartLobbyDto>()) {
            val lobby = lobbies[lobbyId]!!
            if (
                !lobby.isOwner(sessionService.getUserFromSessionId(sessionId!!)!!)
            ) throw NotOwner()
            lobby.start()
        }
    }

    private fun getAvailableLobbies(): AvailableLobbiesDto {
        return AvailableLobbiesDto(
            arrayOf(
                *lobbies.values.filter {
                    !it.isStarted
                }.map {
                    it.toDto()
                }.toTypedArray()
            )
        )
    }

    private fun joinLobby(message: String): LobbyBase {
        with(message.toDto<JoinLobbyDto>()) {
            if (lobbies.keys.contains(lobbyId)) {
                lobbies[lobbyId]!!.join(
                    sessionService.getUserFromSessionId(sessionId!!)!!
                )
            } else {
                throw InvalidLobbyId()
            }
            return lobbies[lobbyId]!!.toDto()
        }
    }

    private fun createLobby(message: String): LobbyBase {
        val dto: CreateLobbyDto = message.toDto()

        val lobby = Lobby(
            createLobbyId().toString(),
            dto.size,
            sessionService.getUserFromSessionId(dto.sessionId!!)!!,
            "${sessionService.getUserFromSessionId(dto.sessionId!!)!!}'s room.",
            sessionService,
            gameService
        )
        lobbies[lobby.lobbyId] = lobby
        return lobby.toDto()
    }

    private fun createLobbyId(): Int {
        var randomNumber: Int
        do {
            randomNumber = Random.nextInt(100000, 1000000)
        } while (lobbies.containsKey(randomNumber.toString()))
        return randomNumber
    }
}
