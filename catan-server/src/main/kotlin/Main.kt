import database.DatabaseService
import game.GameService
import lobby.LobbyService
import login.LoginService
import register.RegisterService
import service.*
import session.SessionService
import socket.SocketController
import socket.SocketService
import statistics.StatisticsService

fun main() {
    val databaseService = DatabaseService()
    val sessionService = SessionService()
    val statisticsService = StatisticsService(databaseService)
    val gameService = GameService(sessionService, databaseService)
    val lobbyService = LobbyService(sessionService, gameService)
    val loginService = LoginService(databaseService, sessionService)
    val registerService = RegisterService(databaseService)
    val socketService = SocketService(
        loginService,
        registerService,
        lobbyService,
        gameService,
        sessionService,
        statisticsService
    )
    val sc = SocketController(
        socketService,
        8000
    )
    sc.startController()
}
