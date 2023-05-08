package controller

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.STARTUP
import com.catan.sdk.dto.game.out.StartupDto
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
    var refreshView : (() -> Unit)? = null

    private fun startup(dto: StartupDto) {
        dto.players.forEach {
            players[it] = Player(it, RED)
        }
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        Platform.runLater {
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun passTheTurn() {

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
        }
        map.edges.forEach {
            if (it.id == "E30") {
                println("alamfa")
                it.owner = player
            }
        }
    }

    fun handle(message: String) {
        with(message.toDto<Game>()) {
            when (gameType) {
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

    }

    fun useMonopoly(resource: ResourceType) {

    }

    fun setStartVillage(vertexId: String) {
        val v = map.vertexes.find { it.id == vertexId } ?: return
        chosenVertexAtBeginning = v
        v.owner = me
        v.buildingType = BuildType.VILLAGE
        state = GameState.StartPlaceRoad
    }

    fun getRoadPlacementForBeginning() = chosenVertexAtBeginning!!.edges

    fun knights() = me!!.cards.filter { it.developmentTypes == Knight }


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
