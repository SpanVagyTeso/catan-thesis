package controller

import com.catan.sdk.dto.*
import com.catan.sdk.dto.game.*
import com.catan.sdk.dto.game.fromclient.*
import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.Pass
import com.catan.sdk.dto.lobby.*
import com.catan.sdk.dto.login.LoginDto
import com.catan.sdk.dto.login.LoginSuccessDto
import com.catan.sdk.dto.register.RegisterDto
import com.catan.sdk.dto.stats.GetStatsDto
import com.catan.sdk.dto.stats.StatsDto
import com.catan.sdk.entities.ResourceType
import com.catan.sdk.entities.TradeType
import com.catan.sdk.toDto
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
    var stats: StatsDto? = null

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

    fun startLobby() {
        socket.sendDto(
            StartLobbyDto(
                sessionId,
                currentLobby.sessionId!!
            )
        )
    }

    fun leaveLobby() {
        socket.sendDto(
            LeaveLobbyDto(
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

    fun getStats(username: String) {
        socket.sendDto(
            GetStatsDto(
                username
            )
        )
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

    fun sendDto(payload: FromClient) {
        socket.sendDto(
            payload
        )
    }

    fun sendPass() {
        socket.sendDto(
            FromClient(
                sessionId,
                Pass
            )
        )
    }

    fun sendMaritimeTrade(res1: ResourceType, res2: ResourceType, tradeType: TradeType) {
        socket.sendDto(
            MaritimeTradeDto(
                sessionId,
                res1,
                res2,
                tradeType
            )
        )
    }

    fun sendTwoRoad(edgeId1: String, edgeId2: String) {
        socket.sendDto(
            TwoRoadDto(
                sessionId,
                edgeId1,
                edgeId2
            )
        )
    }

    fun sendTradeOffer(
        myResource: ResourceType,
        amount: Int,
        toResource: ResourceType,
        toAmount: Int,
        toWho: String
    ) {
        socket.sendDto(
            PlayerTradeDto(
                sessionId,
                toWho,
                myResource,
                amount,
                toResource,
                toAmount,
            )
        )
    }

    fun acceptTrade(id: Int) {
        socket.sendDto(
            AcceptTrade(
                sessionId,
                id
            )
        )
    }

    fun sendBuy(type: BuyType, id: String? = null) {
        socket.sendDto(
            BuyDto(
                sessionId,
                type,
                id
            )
        )
    }

    fun sendYearOfPlenty(res1: ResourceType, res2: ResourceType) {
        socket.sendDto(
            YearOfPlentyDto(
                sessionId,
                res1,
                res2
            )
        )
    }

    fun sendMonopoly(res: ResourceType) {
        socket.sendDto(
            MonopolyDto(
                sessionId,
                res
            )
        )
    }

    fun sendBeginning(edgeId: String, tileId: String) {
        socket.sendDto(
            PlaceBeginningDto(
                sessionId,
                edgeId,
                tileId
            )
        )
    }

    fun sendSteal(id: String, isKnight: Boolean, fromWho: String?) {
        socket.sendDto(
            StealDto(sessionId, id, isKnight, fromWho)
        )
    }

    private fun parseLobbies(msg: String) {
        lobbies.clear()
        with(msg.toDto<AvailableLobbiesDto>()) {
            this@ViewController.lobbies = this.lobbies.toMutableList()
        }
    }

    private fun handleStats(statsDto: StatsDto) {
        stats = statsDto
        Platform.runLater {
            refreshCurrentView()
        }

    }

    private fun lobbyInfo(msg: String) {
        currentLobby = msg.toDto()
        if (currentLobby.users.contains(currentLobby.ownerUser)) {
            Platform.runLater {
                currentView.replaceWith<LobbyView>()
                refreshCurrentView()
            }
        } else {
            leaveLobby()
            Platform.runLater {
                currentView.replaceWith<LobbySelectionView>()
                refreshCurrentView()
            }
        }

    }

    private fun socketMessageHandler(msg: String) {
        println("RECEIVED: ${msg}")
        with(msg.toDto<DtoType>()) {
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
                                lobbyInfo(msg)
                            }
                        }
                    }
                }

                GAME -> {
                    gameController.handle(msg)
                }

                STATS -> {
                    handleStats(msg.toDto())
                }

                else -> {
                    println("Unhandled dto: ${type}")
                }
            }
        }
    }
}
