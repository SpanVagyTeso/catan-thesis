package controller

import com.catan.sdk.dto.game.fromclient.BuyType
import com.catan.sdk.dto.game.fromserver.*
import com.catan.sdk.dto.game.fromserver.ChangeType.*
import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.*
import com.catan.sdk.entities.*
import com.catan.sdk.entities.Map
import com.catan.sdk.toDto
import controller.GameState.*
import gui.custom.corners
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
    var firstOfTwoRoad: Edge? = null
    var refreshView: (() -> Unit)? = null
    var showWinners: (() -> Unit)? = null
    var gameAbrubtlyEnded: (() -> Unit)? = null
    var remainingDevelopmentCards = 0
    var winners = mutableListOf<Player>()
    var offers = mutableListOf<PlayerTradeOfferDto>()
    var developmentPlayed = false

    private fun startup(dto: StartupDto) {
        dto.players.forEach {
            players[it.userName] = Player(it)
        }
        map.loadFromDto(dto.map)
        map.attachAllTiles()
        currentPlayer = players[dto.startingPlayer]!!
        me = players[viewController.username]!!
        state = Start
        corners.clear()
        println("STARTING FOR REAL")
        Platform.runLater {
            viewController.currentView.replaceWith<GameView>()
            viewController.refreshCurrentView()
        }
    }

    fun passTheTurn() {
        viewController.sendPass()
        developmentPlayed = false
    }

    fun canBuySettlement(): Boolean {
        if (me!!.resources[ResourceType.Wool]!! < 1) return false
        if (me!!.resources[ResourceType.Grain]!! < 1) return false
        if (me!!.resources[ResourceType.Brick]!! < 1) return false
        if (me!!.resources[ResourceType.Lumber]!! < 1) return false
        return getGoodCorners().isNotEmpty()
    }

    fun canBuyCity(): Boolean {
        if (me!!.resources[ResourceType.Ore]!! < 3) return false
        if (me!!.resources[ResourceType.Grain]!! < 2) return false
        return getCurrentPlayerSettlements().isNotEmpty()
    }

    fun canBuyRoad(): Boolean {
        if (me!!.resources[ResourceType.Brick]!! < 1) return false
        if (me!!.resources[ResourceType.Lumber]!! < 1) return false
        return getGoodRoads().isNotEmpty()
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
                    newDices(message.toDto())
                }

                RESOURCES -> resourceChange(message.toDto())
                SEVEN -> sevenRolled()
                CURRENTPLAYER -> newCurrentPlayer(message.toDto())
                DEVELOPMENTCARDSREMAINING -> remainingDevCards(message.toDto())
                WINNERS -> gameIsWon(message.toDto())
                SOMEONELEFT -> someoneLeft()
                TRADEOFFER -> handleOffers(message.toDto())
            }
        }

    }

    private fun handleOffers(dto: PlayerTradeOffersDto) {
        offers.clear()
        dto.offers.forEach {
            offers.add(it)
        }
        refreshView!!()
    }

    private fun remainingDevCards(dto: DevelopmentCardsRemainingDto) {
        remainingDevelopmentCards = dto.remaining
        refreshView!!()
    }

    private fun gameIsWon(winnersDto: WinnersDto) {
        state = GameEnd
        winnersDto.winners.forEach {
            winners.add(players[it]!!)
        }
        showWinners!!()
    }

    private fun someoneLeft() {
        state = GameEnd
        gameAbrubtlyEnded!!()
    }

    fun offerTrade(
        myResource: ResourceType,
        amount: Int,
        toResource: ResourceType,
        toAmount: Int,
        toWho: String
    ) {
        if (me!!.resources[myResource]!! < amount) return
        viewController.sendTradeOffer(
            myResource,
            amount,
            toResource,
            toAmount,
            toWho
        )
    }

    fun atBeginning()  = state == Start ||
        state == StartPlaceRoad ||
        state == StartOther
    fun acceptTrade(id: Int){
        viewController.acceptTrade(id)
    }

    fun changeOnBoard(dto: ChangeOnBoardDto) {
        when (dto.changeType) {
            ROAD -> {
                map.edges.find { it.id == dto.id }!!.owner = players[dto.username]!!
            }

            CITY -> {
                map.vertexes.find { it.id == dto.id }!!.let {
                    it.buildingType = BuildType.CITY
                }

            }

            SETTLEMENT -> {
                map.vertexes.find { it.id == dto.id }!!.let {
                    it.buildingType = BuildType.SETTLEMENT
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
        refreshView!!()
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
        developmentPlayed = false
    }

    fun buyCity(vertexId: String) {
        viewController.sendBuy(
            BuyType.CITY,
            vertexId
        )
    }

    fun buySettlement(vertexId: String) {
        viewController.sendBuy(
            BuyType.SETTLEMENT,
            vertexId
        )

    }

    fun buyEdge(edgeId: String) {
        viewController.sendBuy(
            BuyType.ROAD,
            edgeId

        )
    }

    fun maritimeTrade(resource1: ResourceType, resource2: ResourceType, tradeType: TradeType) {
        viewController.sendMaritimeTrade(
            resource1,
            resource2,
            tradeType
        )
    }

    fun buyUpgrade() {
        viewController.sendBuy(
            BuyType.UPGRADE
        )
    }

    fun useYearsOfPlenty(resource1: ResourceType, resource2: ResourceType) {
        viewController.sendYearOfPlenty(resource1, resource2)
        developmentPlayed = true
    }

    fun useMonopoly(resource: ResourceType) {
        viewController.sendMonopoly(resource)
        developmentPlayed = true
    }

    fun steal(tile: Tile, isKnight: Boolean, fromWho: Player?) {
        viewController.sendSteal(tile.id, isKnight, fromWho?.username)
        developmentPlayed = isKnight
    }

    fun setStartSettlement(vertexId: String) {
        val v = map.vertexes.find { it.id == vertexId } ?: return
        chosenVertexAtBeginning = v
        v.owner = me
        v.buildingType = BuildType.SETTLEMENT
        state = StartPlaceRoad
    }

    fun sendStartSettlementAndRoad(edgeId: String) {
        val edge = chosenVertexAtBeginning!!.edges.find { it.id == edgeId }
        if (edge == null) {
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

    fun getGoodRoads(): List<Edge> = map.getBuyableEdges(players[viewController.username]!!)


    fun consumeTwoRoads(edgeId: String) {
        if (firstOfTwoRoad == null) {
            firstOfTwoRoad = map.edges.find { it.id == edgeId } ?: return
            firstOfTwoRoad!!.owner = me
        } else {
            viewController.sendTwoRoad(firstOfTwoRoad!!.id, (map.edges.find { it.id == edgeId } ?: return).id)
            firstOfTwoRoad = null
            state = Normal
            developmentPlayed = true
        }
        refreshView!!()
    }

    fun getCurrentPlayerSettlements() = map.vertexes.filter {
        it.owner == players[viewController.username] && it.buildingType == BuildType.SETTLEMENT
    }

    fun getCurrentPlayerVertexes() = map.vertexes.filter {
        it.owner == players[viewController.username] && (it.buildingType == BuildType.SETTLEMENT || it.buildingType == BuildType.CITY)
    }
}

enum class GameState {
    OtherPlayer,
    Start,
    StartPlaceRoad,
    StartOther,
    BuyCity,
    BuyRoad,
    BuySettlement,
    Normal,
    UseKnight,
    UseRoads,
    UseYearsOfPlenty,
    UseMonopoly,
    BuyMenu,
    MaritimeTradeMenu,
    DevelopmentMenu,
    Stealing,
    Seven,
    GameEnd
}
