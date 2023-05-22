package socket

import com.catan.sdk.dto.*
import com.catan.sdk.dto.error.BadDto
import com.catan.sdk.dto.game.GAME
import com.catan.sdk.toDto
import com.catan.sdk.toJson
import error.InvalidDto
import service.*

class SocketService(
    private val loginService: LoginService,
    private val registerService: RegisterService,
    private val lobbyService: LobbyService,
    private val gameService: GameService,
    private val sessionService: SessionService,
    private val statisticsService: StatisticsService
) {
    suspend fun handleIncomingMessage(socket: SocketConnection, message: String) {
        val dtoType: String
        try {
            dtoType = message.toDto<DtoType>().type
        } catch (e: Exception) {
            throw InvalidDto()
        }


        when (dtoType) {
            LOGIN -> {
                val dto = loginService.login(message, socket)
                socket.sendMessage(dto.toJson())
            }

            REGISTER -> {
                val dto = registerService.register(message)
                socket.sendMessage(dto.toJson())
            }

            LOBBY_BASE -> {
                lobbyService.handleCommand(message, socket)
            }

            GAME -> {
                gameService.handleIncomingMessage(message)
            }

            STATS -> {
                val response = statisticsService.getStats(message.toDto())
                socket.sendMessage(response.toJson())
            }

            else -> {
                socket.sendMessage(BadDto("unknown command").toJson())
            }
        }
    }

    fun closedConnection(socket: SocketConnection) {
        val infos = sessionService.getUsernameFromSocket(socket) ?: return
        lobbyService.closedSocket(infos.username)
        gameService.socketClosed(infos.username)
    }


}
