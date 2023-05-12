package controller

import com.catan.sdk.dto.game.*
import com.catan.sdk.dto.game.fromclient.*
import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.Pass
import com.catan.sdk.dto.game.fromserver.*
import com.catan.sdk.dto.game.fromserver.ChangeType.*
import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.*
import com.catan.sdk.entities.*
import com.catan.sdk.entities.DevelopmentTypes.Knight
import com.catan.sdk.entities.Map
import com.catan.sdk.entities.PlayerColor.*
import com.catan.sdk.toDto
import controller.GameState.*
import gui.views.GameView
import javafx.application.Platform

class GameController(
    private val viewController: ViewController
) {
    val map = Map()
    val players = mutableMapOf<String, Player>()
    var me: Player? = null
    var currentPlayer: Player? = null
    var dices = 0 to 0
    var state = Start
        set(value) {
            field = value
            refreshView?.let { it() }
        }
    var chosenVertexAtBeginning: Vertex? = null
    var refreshView: (() -> Unit)? = null

    private fun startup(dto: StartupDto) {
        println("STARTING")
        dto.players.forEach {
            players[it.userName] = Player(it)
        }
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        currentPlayer = players[dto.startingPlayer]!!
        me = players[viewController.username]!!

        println("STARTING FOR REAL")
        Platform.runLater {
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun passTheTurn() {
        viewController.sendPass()
    }

    fun test() {
        map.generateTiles()
        map.attachAllTiles()
        var counter = 0
        val player = Player(viewController.username, RED)
        currentPlayer = player
        player.cards.add(DevelopmentCard(Knight, false))
        viewController.username = player.username
        val player1 = Player(viewController.username + "1", GREEN)
        val player2 = Player(viewController.username + "2", BLUE)
        me = player

        players[viewController.username] = player
        players[player1.username] = player1
        players[player2.username] = player2
        map.tiles.forEach {
            it.rolledNumber = counter % 11 + 2
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
            it.vertices.find {
                it?.id == "V2"
            }?.apply {
                owner = player1
                buildingType = BuildType.CITY
                edges[0].owner = player

            }
            it.vertices.find {
                it?.id == "V3"
            }?.apply {
                owner = player2
                buildingType = BuildType.CITY
                edges[0].owner = player

            }
            if (it.id == "T5") {
                it.isBlocked = true
            }
        }
        map.edges.forEach {
            if (it.id == "E30") {
                it.owner = player
            }
        }
    }

    fun canBuyVillage(): Boolean {
        if (me!!.resources[ResourceType.Wool]!! < 1) return false
        if (me!!.resources[ResourceType.Grain]!! < 1) return false
        if (me!!.resources[ResourceType.Brick]!! < 1) return false
        if (me!!.resources[ResourceType.Lumber]!! < 1) return false
        return getGoodCorners().isNotEmpty()
    }

    fun canBuyCity(): Boolean {
        if (me!!.resources[ResourceType.Ore]!! < 3) return false
        if (me!!.resources[ResourceType.Grain]!! < 2) return false
        return getCurrentPlayerVillages().isNotEmpty()
    }

    fun canBuyRoad(): Boolean {
        if (me!!.resources[ResourceType.Brick]!! < 1) return false
        if (me!!.resources[ResourceType.Lumber]!! < 1) return false
        return getCurrentPlayerVillages().isNotEmpty()
    }

    fun canBuyDevelopment(): Boolean {
        if (me!!.resources[ResourceType.Ore]!! < 1) return false
        if (me!!.resources[ResourceType.Wool]!! < 1) return false
        return me!!.resources[ResourceType.Grain]!! > 0
    }

    fun handle(message: String) {
        println("Incoming message: $message")
        with(message.toDto<FromServer>()) {
            println("TYPE: $payloadType")
            println(message)
            when (payloadType) {
                STARTUP -> {
                    startup(message.toDto())
                }
                CHANGEONBOARD -> {
                    changeOnBoard(message.toDto())
                }
                DICES -> {
                    println("ANYAD3")
                    newDices(message.toDto())
                }
                RESOURCES -> resourceChange(message.toDto())
                SEVEN -> sevenRolled()
                CURRENTPLAYER -> newCurrentPlayer(message.toDto())
            }
        }

    }

    private fun changeOnBoard(dto: ChangeOnBoardDto) {
        when (dto.changeType) {
            ROAD -> {
                map.edges.find { it.id == dto.id }!!.owner = players[dto.username]!!
            }

            CITY -> {
                map.vertexes.find { it.id == dto.id }!!.let {
                    it.buildingType = BuildType.CITY
                }

            }

            VILLAGE -> {
                map.vertexes.find { it.id == dto.id }!!.let {
                    it.buildingType = BuildType.VILLAGE
                    it.owner = players[dto.username]!!
                }
            }

            REMOVEBLOCK -> {
                map.tiles.find {
                    it.id == dto.id
                }!!.isBlocked = false
            }

            ADDBLOCK -> {
                map.tiles.find {
                    it.id == dto.id
                }!!.isBlocked = true
            }
        }
        refreshView!!()
    }

    private fun newDices(newDices: DiceRollDto) {
        dices = newDices.dice1 to newDices.dice2
        refreshView!!()
    }

    private fun resourceChange(dto: ResourcesDto) {
        dto.players.forEach {
            players[it.userName]!!.refreshFromDto(it)
        }
    }

    private fun sevenRolled() {
        state = Seven
        refreshView!!()
    }

    private fun newCurrentPlayer(dto: CurrentPlayerDto) {
        currentPlayer = players[dto.userName]!!
        state = if (currentPlayer != me) {
            if (dto.isStarted) {
                OtherPlayer
            } else {
                StartOther
            }
        } else {
            if (dto.isStarted) {
                Normal
            } else {
                Start
            }
        }
        refreshView!!()
        println("done")
    }

    fun buyCity(vertexId: String) {
        viewController.sendBuy(
            BuyType.CITY,
            vertexId
        )
    }

    fun buyVillage(vertexId: String) {
        viewController.sendBuy(
            BuyType.VILLAGE,
            vertexId

        )

    }

    fun buyEdge(edgeId: String) {
        viewController.sendBuy(
            BuyType.ROAD,
            edgeId

        )
    }

    fun buyUpgrade() {
        viewController.sendBuy(
            BuyType.UPGRADE
        )
    }

    fun useYearsOfPlenty(resource1: ResourceType, resource2: ResourceType) {
        viewController.sendYearOfPlenty(resource1, resource2)
    }

    fun useMonopoly(resource: ResourceType) {
        viewController.sendMonopoly(resource)
    }

    fun steal(tile: Tile, isKnight: Boolean, fromWho: Player?) {
        viewController.sendSteal(tile.id, false, fromWho?.username)
    }

    fun setStartVillage(vertexId: String) {
        val v = map.vertexes.find { it.id == vertexId } ?: return
        chosenVertexAtBeginning = v
        v.owner = me
        v.buildingType = BuildType.VILLAGE
        state = StartPlaceRoad
    }

    fun sendStartVillageAndRoad(edgeId: String) {
        val edge = chosenVertexAtBeginning!!.edges.find { it.id == edgeId }
        if(edge == null) {
            chosenVertexAtBeginning = null
            state = Start
            refreshView!!()
            return
        }
        chosenVertexAtBeginning!!.owner = null
        chosenVertexAtBeginning!!.buildingType = null
        viewController.sendBeginning(edgeId, chosenVertexAtBeginning!!.id)
        state = Start
    }

    fun getRoadPlacementForBeginning() = chosenVertexAtBeginning!!.edges

    fun otherPlayers() = players.filter { it.key != viewController.username }

    fun getGoodCorners(ignoreRoad: Boolean = false) =
        map.getBuyableVertexes(players[viewController.username]!!, ignoreRoad)

    fun getGoodRoads() = map.getBuyableEdges(players[viewController.username]!!)

    fun getCurrentPlayerVillages() = map.vertexes.filter {
        it.owner == players[viewController.username] && it.buildingType == BuildType.VILLAGE
    }
}

enum class GameState {
    OtherPlayer,
    Start,
    StartPlaceRoad,
    StartOther,
    BuyCity,
    BuyRoad,
    BuyVillage,
    Normal,
    UseKnight,
    UseRoads,
    UseYearsOfPlenty,
    UseMonopoly,
    BuyMenu,
    DevelopmentMenu,
    Stealing,
    Seven
}
