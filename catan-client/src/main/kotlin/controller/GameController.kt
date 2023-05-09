package controller

import com.catan.sdk.dto.game.*
import com.catan.sdk.dto.game.fromserver.FromServer
import com.catan.sdk.dto.game.fromserver.FromServerPayloadType
import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.STARTUP
import com.catan.sdk.dto.game.fromserver.StartupDto
import com.catan.sdk.entities.*
import com.catan.sdk.entities.DevelopmentTypes.Knight
import com.catan.sdk.entities.Map
import com.catan.sdk.entities.PlayerColor.*
import com.catan.sdk.toDto
import gui.views.GameView
import javafx.application.Platform

class GameController(
    private val viewController: ViewController
) {
    val map = Map()
    val players = mutableMapOf<String, Player>()
    var me: Player? = null
    var currentPlayer: Player? = null
    var state = GameState.Normal
        set(value) {
            field = value
            refreshView?.let { it() }
        }
    var chosenVertexAtBeginning: Vertex? = null
    var refreshView: (() -> Unit)? = null

    private fun startup(dto: StartupDto) {
        dto.players.forEach {
            players[it.userName] = Player(it)
        }
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        Platform.runLater {
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun passTheTurn() {
        //send pass
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
        with(message.toDto<FromServer>()) {
            when (this.payload.type) {
                STARTUP -> {
                    startup(message.toDto())
                }
            }
        }
    }

    fun buyCity(vertexId: String) {
        viewController.buyCity(vertexId)
    }

    fun buyVillage(vertexId: String) {
        viewController.buyVillage(vertexId)

    }

    fun buyEdge(edgeId: String) {
        viewController.buyRoad(edgeId)
    }

    fun buyUpgrade() {
        viewController.buyUpgrade()
    }

    fun useYearsOfPlenty(resource1: ResourceType, resource2: ResourceType) {
        YearOfPlentyDto(resource1, resource2)
    }

    fun useMonopoly(resource: ResourceType) {
        MonopolyDto(resource)
    }

    fun steal(tile: Tile, isKnight: Boolean, fromWho: Player?) {
        StealDto(tile.id, false, fromWho?.username)
    }

    fun setStartVillage(vertexId: String) {
        val v = map.vertexes.find { it.id == vertexId } ?: return
        chosenVertexAtBeginning = v
        v.owner = me
        v.buildingType = BuildType.VILLAGE
        state = GameState.StartPlaceRoad
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
