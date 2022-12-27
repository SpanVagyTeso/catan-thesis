package controller

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.LOBBY_BASE
import com.catan.sdk.dto.LOGIN_SUCCESS
import com.catan.sdk.dto.REGISTER_SUCCES
import com.catan.sdk.dto.game.GAME
import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.STARTUP
import com.catan.sdk.dto.game.`in`.BuyDto
import com.catan.sdk.dto.game.`in`.BuyType
import com.catan.sdk.dto.lobby.*
import com.catan.sdk.dto.login.LoginDto
import com.catan.sdk.dto.login.LoginSuccessDto
import com.catan.sdk.dto.register.RegisterDto
import com.catan.sdk.toDto
import gui.views.GameView
import gui.views.LobbySelectionView
import gui.views.LobbyView
import gui.views.LoginView
import javafx.application.Platform
import tornadofx.Controller
import tornadofx.View
import kotlin.concurrent.thread

class ViewController : Controller() {
    lateinit var currentView: View
    lateinit var refreshCurrentView: () -> Unit
    var sessionId = ""
    var username = ""
    private val socket: SocketController = SocketController()
    var lobbies = mutableListOf<LobbyDto>()
    lateinit var currentLobby: LobbyDto
    val gameController = GameController(this)

    private val socketThread: Thread = thread(
        start = false,
        isDaemon = true
    ) {
        socket.run()
    }

    init {
        socket.handler = {
            socketMessageHandler(it)
        }
        socketThread.start()
    }

    fun map() = gameController.map.rows

    fun register(username: String, password: String) {
        socket.sendDto(
            RegisterDto(
                username,
                password
            )
        )
    }

    fun startLobby(){
        socket.sendDto(
            StartLobbyDto(
                sessionId,
                currentLobby.sessionId!!
            )
        )
    }

    fun login(username: String, password: String) {
        socket.sendDto(
            LoginDto(
                username,
                password
            )
        )
        this.username = username
    }

    fun joinLobby(lobbyId: String) {
        socket.sendDto(
            JoinLobbyDto(
                lobbyId,
                sessionId
            )
        )
    }

    fun createLobby() {
        socket.sendDto(
            CreateLobbyDto(
                "almafa",
                4,
                sessionId
            )
        )
    }

    fun sendGetLobbies() {
        socket.sendDto(
            GetLobbiesDto(
                sessionId
            )
        )
    }

    fun buyCity(vertexId: String){
        socket.sendDto(
            BuyDto(
                BuyType.CITY,
                vertexId
            )
        )
    }

    fun buyVillage(vertexId: String){
        socket.sendDto(
            BuyDto(
                BuyType.VILLAGE,
                vertexId
            )
        )
    }

    fun buyRoad(edgeId: String){
        socket.sendDto(
            BuyDto(
                BuyType.ROAD,
                edgeId
            )
        )
    }

    fun buyUpgrade(){
        socket.sendDto(
            BuyDto(
                BuyType.UPGRADE
            )
        )
    }

    private fun parseLobbies(msg: String) {
        lobbies.clear()
        with(msg.toDto<AvailableLobbiesDto>()) {
            this@ViewController.lobbies = this.lobbies.toMutableList()
        }
    }

    private fun socketMessageHandler(msg: String) {
        with(msg.toDto<DtoType>()) {
            println(type)
            println(msg)
            when (type) {
                LOGIN_SUCCESS -> {
                    sessionId = msg.toDto<LoginSuccessDto>().sessionId
                    Platform.runLater {
                        currentView.replaceWith<LobbySelectionView>()
                    }
                }
                REGISTER_SUCCES -> {
                    Platform.runLater {
                        currentView.replaceWith<LoginView>()
                    }
                }
                LOBBY_BASE -> {
                    with(msg.toDto<LobbyBase>()) {
                        when (commandType) {
                            AVAILABLE_LOBBIES -> {
                                parseLobbies(msg)
                                Platform.runLater {
                                    refreshCurrentView()
                                }
                            }
                            LOBBY_INFO -> {
                                currentLobby = msg.toDto()
                                Platform.runLater {
                                    currentView.replaceWith<LobbyView>()
                                    refreshCurrentView()
                                }
                            }
                        }
                    }
                }
                GAME -> {
                    gameController.handle(message = msg)
                }
            }
        }
    }
}