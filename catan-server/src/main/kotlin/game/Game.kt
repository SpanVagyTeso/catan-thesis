package game

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.game.MonopolyDto
import com.catan.sdk.dto.game.StealDto
import com.catan.sdk.dto.game.TwoRoadDto
import com.catan.sdk.dto.game.YearOfPlentyDto
import com.catan.sdk.dto.game.fromclient.*
import com.catan.sdk.dto.game.fromclient.BuyType.*
import com.catan.sdk.dto.game.fromserver.*
import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.SOMEONELEFT
import com.catan.sdk.entities.*
import com.catan.sdk.entities.DevelopmentTypes.Knight
import com.catan.sdk.entities.FieldType.DESERT
import com.catan.sdk.entities.Map
import com.catan.sdk.entities.ResourceType.*
import com.catan.sdk.entities.TradeType.*
import com.catan.sdk.toJson
import game.GameState.*
import kotlinx.coroutines.runBlocking
import database.DatabaseService
import session.SessionService

class Game(
        val players: List<Player>,
        private val sessionService: SessionService,
        private val databaseService: DatabaseService,
        private val diceRoller: DiceRoller = DiceRoller(),
        private val settlementsAtBeginning: Int = 2,
        private val roadsAtBeginning: Int = 2,
        private val maxSettlements: Int = 5,
        private val maxRoads: Int = 15,
        private val maxCities: Int = 5,
        private val minimumForLongestRoad: Int = 5,
        private val minimumForMostKnights: Int = 3,
        private val developmentPool: DevelopmentPool = DevelopmentPool(
        14,
        2,
        2,
        2,
        1,
        1,
        1,
        1,
        1
    ),
        private val pointsToWin: Int = 10,
        currentPlayerIndex: Int = 0
) {

    var currentPlayer: Player
    val map: Map = Map()
    private var beginningPlacementsRoad: MutableMap<String, Int>
    private val beginningPlacementsSettlement: MutableMap<String, Int>
    var gameState = Anything
    private var developmentPlayed = false
    var atBeginning = true
        private set

    var ownerOfLongestRoad: Player? = null
    var winners = mutableListOf<Player>()
    var currentTradeOffers = mutableListOf<PlayerTradeDto>()


    init {
        currentPlayer = players[currentPlayerIndex]

        map.generateTiles(5)
        map.attachAllTiles()
        beginningPlacementsRoad = players.associate {
            it.username to roadsAtBeginning
        } as MutableMap<String, Int>
        beginningPlacementsSettlement = players.associate {
            it.username to settlementsAtBeginning
        } as MutableMap<String, Int>
    }

    private fun sendPlayers(dto: DtoType) {
        players.forEach {
            runBlocking {
                sessionService.getSessionInfoFromUsername(it.username).socket.sendMessage(
                    dto.toJson()
                )
            }
        }
    }

    fun spendResources(dto: BuyDto) {
        if (atBeginning) return
        if (gameState != Anything) return
        println("Buying type: ${dto.buyType}")
        when (dto.buyType) {
            ROAD -> {
                if (dto.id == null) return
                buyRoad(dto.id!!)
            }

            SETTLEMENT -> {
                if (dto.id == null) return
                buySettlement(dto.id!!)
            }

            CITY -> {
                if (dto.id == null) return
                buyCity(dto.id!!)
            }

            UPGRADE -> buyUpgrade()
        }
        sendAllPlayersResources()
    }

    fun placeAtBeginning(dto: PlaceBeginningDto) {
        if (!atBeginning) {
            println("Game is already started")
            return
        }
        if (gameState != Anything) {
            println("Game is in a wierd state")
            return
        }
        var anyLeft = false

        beginningPlacementsSettlement.forEach {
            anyLeft = anyLeft || (it.value != 0)
        }

        checkLocations(dto.edgeId, dto.vertexId)
        val outcome = placeSettlementAtBeginning(dto.vertexId) && placeRoadAtBeginning(dto.edgeId)
        if (!outcome) {
            println("Something went wrong with beginning placements... ")
        }
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                dto.edgeId,
                ChangeType.ROAD,
                currentPlayer.username

            ).toJson()
        })
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                dto.vertexId,
                ChangeType.SETTLEMENT,
                currentPlayer.username

            ).toJson()
        })

        anyLeft = false
        beginningPlacementsRoad.forEach {
            anyLeft = anyLeft || (it.value != 0)
        }
        if (!anyLeft) {
            atBeginning = false
        }

        if (atBeginning) {
            val currentIndex = players.indexOf(currentPlayer)
            currentPlayer = players[if (currentIndex == players.size - 1) 0 else currentIndex + 1]
            sendPlayers(CurrentPlayerDto(currentPlayer.username, !atBeginning))
        } else {
            giveResourcesAfterInitialPlacings()
            nextPlayer()
        }
    }

    fun nextPlayer() {
        if (gameState != Anything) return
        val currentIndex = players.indexOf(currentPlayer)
        currentPlayer = players[if (currentIndex == players.size - 1) 0 else currentIndex + 1]
        developmentPlayed = false
        sendPlayers(CurrentPlayerDto(currentPlayer.username, !atBeginning))
        rollTheDice()
        enableDevelopments()
        sendAllPlayersResources()
        sendRemainingDevelopmentCardCount()
        currentTradeOffers.clear()

    }

    fun playerLeft(username: String) {
        players.find {
            it.username == username
        } ?: return
        gameState = Ended
        sendPlayers(
            FromServer(SOMEONELEFT)
        )
    }

    fun maritimeTrade(tradeDto: MaritimeTradeDto) {
        if (tradeDto.fromResource == tradeDto.toResource) return
        when (tradeDto.tradeType) {
            FourToOne -> fourToOneTrade(tradeDto)
            ThreeToOne -> threeToOneTrade(tradeDto)
            else -> twoToOne(tradeDto)
        }
        sendAllPlayersResources()
    }

    fun handleTradeOffer(dto: PlayerTradeDto) {
        if(currentPlayer.resources[dto.resource]!! < dto.amount) return
        if(dto.withWho == currentPlayer.username) return
        currentTradeOffers.add(dto)
        sendAllPlayersResources()
    }

    fun acceptOffer(dto: AcceptTrade, username: String) {
        if(dto.id >= currentTradeOffers.size) return
        val trade = currentTradeOffers[dto.id]
        if (trade.withWho != username) return
        val toPlayer = players.find { it.username == trade.withWho } ?: return
        if (toPlayer.resources[trade.toResource]!! < trade.toAmount) return
        currentPlayer.resources[trade.resource] = (currentPlayer.resources[trade.resource] ?: 0 ) - trade.amount
        toPlayer.resources[trade.toResource] = (toPlayer.resources[trade.toResource] ?: 0 ) - trade.toAmount

        currentPlayer.resources[trade.toResource] = (currentPlayer.resources[trade.toResource] ?: 0 ) + trade.toAmount
        toPlayer.resources[trade.resource] = (toPlayer.resources[trade.resource] ?: 0 ) + trade.amount
        currentTradeOffers.clear()
        sendAllPlayersResources()
    }

    //region Maritime Trade stuff
    private fun fourToOneTrade(tradeDto: MaritimeTradeDto) {
        if (currentPlayer.resources[tradeDto.fromResource]!! < 4) return
        currentPlayer.resources[tradeDto.fromResource] = currentPlayer.resources[tradeDto.fromResource]!! - 4
        currentPlayer.resources[tradeDto.toResource] = currentPlayer.resources[tradeDto.toResource]!! + 1

    }

    private fun threeToOneTrade(tradeDto: MaritimeTradeDto) {
        if (currentPlayer.resources[tradeDto.fromResource]!! < 3) return
        currentPlayer.resources[tradeDto.fromResource] = currentPlayer.resources[tradeDto.fromResource]!! - 3
        currentPlayer.resources[tradeDto.toResource] = currentPlayer.resources[tradeDto.toResource]!! + 1

    }

    private fun twoToOne(tradeDto: MaritimeTradeDto) {
        if (currentPlayer.resources[tradeDto.fromResource]!! < 2) return
        val goodTrade = when (tradeDto.tradeType) {
            TwoToOneWood -> tradeDto.fromResource == Lumber || tradeDto.toResource == Lumber
            TwoToOneSheep -> tradeDto.fromResource == Wool || tradeDto.toResource == Wool
            TwoToOneBrick -> tradeDto.fromResource == Brick || tradeDto.toResource == Brick
            TwoToOneOre -> tradeDto.fromResource == Ore || tradeDto.toResource == Ore
            TwoToOneWheat -> tradeDto.fromResource == Grain || tradeDto.toResource == Grain
            else -> false
        }
        if (!goodTrade) return
        currentPlayer.resources[tradeDto.fromResource] = currentPlayer.resources[tradeDto.fromResource]!! - 2
        currentPlayer.resources[tradeDto.toResource] = currentPlayer.resources[tradeDto.toResource]!! + 1

    }

    //endregion

    //region Buy stuff
    private fun buyUpgrade() {
        if (currentPlayer.resources[Grain]!! < 1) {
            println("Not enough Grain")
            return
        }
        if (currentPlayer.resources[Ore]!! < 1) {
            println("Not enough Ore")
            return
        }
        if (currentPlayer.resources[Wool]!! < 1) {
            println("Not enough Wool")
            return
        }
        println("Buying development")

        val cardType = developmentPool.draw() ?: return
        println("Card type: $cardType")

        currentPlayer.resources[Grain] = currentPlayer.resources[Grain]!! - 1
        currentPlayer.resources[Wool] = currentPlayer.resources[Wool]!! - 1
        currentPlayer.resources[Ore] = currentPlayer.resources[Ore]!! - 1
        currentPlayer.cards.add(
            DevelopmentCard(
                cardType,
                true
            )
        )
        sendRemainingDevelopmentCardCount()
    }

    private fun buyRoad(id: String) {
        println("Buying a road!")
        if (currentPlayer.resources[Lumber]!! < 1) {
            println("Not enough Lumber")
            return
        }
        if (currentPlayer.resources[Brick]!! < 1) {
            println("Not enough Brick")
            return
        }
        val edge = map.edges.find {
            it.id == id
        } ?: return
        if (!edge.canBeOwned) {
            println("Edge cannot be owned")
            return
        }
        if (edge.owner != null) {
            println("Edge has an owner")
            return
        }

        checkIfRoadCanBePlaced(edge)

        if (!placeRoad(edge)) {
            println("Something went wrong")
            return
        }

        currentPlayer.resources[Lumber] = currentPlayer.resources[Lumber]!! - 1
        currentPlayer.resources[Brick] = currentPlayer.resources[Brick]!! - 1
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                edge.id,
                ChangeType.ROAD,
                currentPlayer.username

            ).toJson()
        })
        sendAllPlayersResources()
    }

    private fun buyCity(id: String) {
        if (currentPlayer.resources[Ore]!! < 3) return
        if (currentPlayer.resources[Grain]!! < 2) return
        val tile = map.tiles.find {
            it.vertices.find {
                it?.id == id
            } != null
        } ?: return
        val vertex = tile.vertices.find {
            it?.id == id
        } ?: return

        if (vertex.owner != currentPlayer) return

        if (!placeCity(vertex)) {
            return
        }
        currentPlayer.resources[Ore] = currentPlayer.resources[Ore]!! - 3
        currentPlayer.resources[Grain] = currentPlayer.resources[Grain]!! - 2

        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                vertex.id,
                ChangeType.CITY,
                currentPlayer.username
            ).toJson()
        })
        sendAllPlayersResources()
    }

    private fun buySettlement(id: String) {
        if (currentPlayer.resources[Lumber]!! < 1) return           //Todo
        if (currentPlayer.resources[Brick]!! < 1) return            //Todo
        if (currentPlayer.resources[Wool]!! < 1) return             //Todo
        if (currentPlayer.resources[Grain]!! < 1) return            //Todo
        val tile = map.tiles.find {
            it.vertices.find {
                it?.id == id
            } != null
        } ?: return
        val vertex = tile.vertices.find {
            it?.id == id
        } ?: return

        if (vertex.owner != null) return

        vertex.edges.find {
            it.owner == currentPlayer
        } ?: return

        if (!placeSettlement(vertex)) return

        currentPlayer.resources[Lumber] = currentPlayer.resources[Lumber]!! - 1
        currentPlayer.resources[Brick] = currentPlayer.resources[Brick]!! - 1
        currentPlayer.resources[Wool] = currentPlayer.resources[Wool]!! - 1
        currentPlayer.resources[Grain] = currentPlayer.resources[Grain]!! - 1

        checkForLongestRoad()
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                vertex.id,
                ChangeType.SETTLEMENT,
                currentPlayer.username
            ).toJson()
        })
        sendAllPlayersResources()
    }
    //endregion

    //region Beginning stuff

    private fun giveResourcesAfterInitialPlacings() {
        map.tiles.forEach { tile ->
            if (tile.type != DESERT) {
                tile.vertices.forEach {
                    if (it?.owner != null) {
                        it.owner!!.resources[tile.type.toResourceType()!!] =
                            (it.owner!!.resources[tile.type.toResourceType()] ?: 0) + 1
                    }
                }
            }
        }
        players.forEach { player ->
            ResourceType.values().forEach {
                player.resources[it] = 30
            }
        }
    }

    private fun checkLocations(edgeId: String, vertexId: String): Boolean {
        if (map.vertexes.count { it.id == vertexId && it.owner != null } > 0) return false
        if (map.edges.count { it.id == edgeId && it.owner != null } > 0) return false
        val v = map.vertexes.find { it.id == vertexId }
        if (v == null) {
            println("Cannot find vertex $vertexId")
            return false
        }
        if (v.edges.find { it.id == edgeId } == null) {
            println("These arent next to each other $edgeId, $vertexId")
            return false
        }
        return true
    }

    private fun placeSettlementAtBeginning(id: String): Boolean {
        if (beginningPlacementsSettlement[currentPlayer.username]!! < 1) return false
        val tile = map.tiles.find {
            it.vertices.find {
                it?.id == id
            } != null
        } ?: return false
        val vertex = tile.vertices.find {
            it?.id == id
        } ?: return false

        if (!placeSettlement(vertex)) return false
        beginningPlacementsSettlement[currentPlayer.username] =
            beginningPlacementsSettlement[currentPlayer.username]!! - 1
        println("Placing settlement at beginning for ${currentPlayer.username} remaining ${beginningPlacementsSettlement[currentPlayer.username]}")
        return true
    }

    private fun placeRoadAtBeginning(id: String): Boolean {
        if (beginningPlacementsRoad[currentPlayer.username]!! < 1) return false
        val edge = map.edges.find {
            it.id == id
        } ?: return false
        if (edge.owner != null) return false
        if (edge.endPoints.first.owner != currentPlayer && edge.endPoints.second.owner != currentPlayer) return false

        if (!placeRoad(edge)) return false
        beginningPlacementsRoad[currentPlayer.username] = beginningPlacementsRoad[currentPlayer.username]!! - 1
        println("Placing road at beginning for ${currentPlayer.username} remaining ${beginningPlacementsRoad[currentPlayer.username]}")
        return true
    }

    //endregion

    //region Placing stuff
    private fun placeSettlement(vertex: Vertex): Boolean {
        if (vertex.owner != null) return false
        if (currentPlayer.buildings[BuildType.SETTLEMENT]!! == maxSettlements) return false
        vertex.owner = currentPlayer
        vertex.buildingType = BuildType.SETTLEMENT
        currentPlayer.buildings[BuildType.SETTLEMENT] = currentPlayer.buildings[BuildType.SETTLEMENT]!! + 1
        return true
    }

    private fun placeCity(vertex: Vertex): Boolean {
        if (vertex.owner != currentPlayer && vertex.buildingType != BuildType.SETTLEMENT) return false
        if (currentPlayer.buildings[BuildType.CITY]!! == maxCities) return false
        vertex.owner = currentPlayer
        vertex.buildingType = BuildType.CITY
        currentPlayer.buildings[BuildType.SETTLEMENT] = currentPlayer.buildings[BuildType.SETTLEMENT]!! - 1
        currentPlayer.buildings[BuildType.CITY] = currentPlayer.buildings[BuildType.CITY]!! + 1
        return true
    }

    private fun checkIfRoadCanBePlaced(edge: Edge, pseudoEdge: Edge? = null) = listOf(
        edge.endPoints.first,
        edge.endPoints.second
    ).any {
        it.owner == currentPlayer || it.edges.any { it.owner == currentPlayer || it == pseudoEdge }
    }

    private fun placeRoad(edge: Edge): Boolean {
        if (edge.owner != null) return false
        if (currentPlayer.buildings[BuildType.ROAD]!! == maxRoads) return false
        edge.owner = currentPlayer
        currentPlayer.buildings[BuildType.ROAD] = currentPlayer.buildings[BuildType.ROAD]!! + 1
        checkForLongestRoad()
        return true
    }
    //endregion

    //region Development stuff
    fun useMonopoly(monopolyDto: MonopolyDto) {
        if (developmentPlayed) return
        val resourceType = monopolyDto.resourceType
        var allOfTheResources = 0
        if (currentPlayer.getMonopolies().none { !it.hasToWait && !it.used }) {
            return
        }
        players.forEach {
            if (it != currentPlayer) {
                allOfTheResources += it.resources[resourceType]!!
                it.resources[resourceType] = 0
            }
        }
        val a = currentPlayer.getMonopolies().find {
            !it.hasToWait && !it.used
        } ?: return
        a.used = true
        currentPlayer.resources[resourceType] = currentPlayer.resources[resourceType]!! + allOfTheResources
        sendAllPlayersResources()
        developmentPlayed = true
    }

    fun useYearOfPlenty(yearOfPlentyDto: YearOfPlentyDto) {
        if (developmentPlayed) return
        if (currentPlayer.getYearOfPlenties().none { !it.hasToWait && !it.used }) {
            return
        }
        val a = currentPlayer.getYearOfPlenties().find {
            !it.hasToWait && !it.used
        } ?: return
        a.used = true
        currentPlayer.resources[yearOfPlentyDto.resource1] = currentPlayer.resources[yearOfPlentyDto.resource1]!! + 1
        currentPlayer.resources[yearOfPlentyDto.resource2] = currentPlayer.resources[yearOfPlentyDto.resource2]!! + 1
        sendAllPlayersResources()
        developmentPlayed = true
    }

    fun useTwoRoad(twoRoadDto: TwoRoadDto) {
        if (developmentPlayed) return
        if (currentPlayer.buildings[BuildType.ROAD]!! == maxRoads) {
            println("No roads remaining")
            return
        } else if (currentPlayer.buildings[BuildType.ROAD]!! + 1 == maxRoads && twoRoadDto.edgeId2 != null) {
            println("No roads remaining")
            return
        }
        if (currentPlayer.getRoadBuildings().none { !it.hasToWait && !it.used }) {
            println("Not enough Road Buildings")
            return
        }
        val edge1 = map.edges.find { it.id == twoRoadDto.edgeId1 } ?: return
        val edge2 = map.edges.find { it.id == twoRoadDto.edgeId2 }
        if (!checkIfRoadCanBePlaced(edge1)) {
            println("Cannot place down edge1")
            return
        }
        if (edge2 != null && !checkIfRoadCanBePlaced(edge2, edge1)) {
            println("Cannot place down edge2")
            return
        }
        edge1.owner = currentPlayer
        currentPlayer.buildings[BuildType.ROAD] = currentPlayer.buildings[BuildType.ROAD]!! + 1
        if (edge2 != null) {
            edge2.owner = currentPlayer
            currentPlayer.buildings[BuildType.ROAD] = currentPlayer.buildings[BuildType.ROAD]!! + 1
        }
        val usedCard = currentPlayer.getRoadBuildings().first { !it.hasToWait && !it.used }
        usedCard.used = true
        sendAllPlayersResources()
        sessionService.sendDatas(
            players.map {
                it to ChangeOnBoardDto(
                    edge1.id,
                    ChangeType.ROAD,
                    currentPlayer.username
                ).toJson()
            })
        if (edge2 != null) {
            sessionService.sendDatas(
                players.map {
                    it to ChangeOnBoardDto(
                        edge2.id,
                        ChangeType.ROAD,
                        currentPlayer.username
                    ).toJson()
                }
            )
        }
        developmentPlayed = true
    }

    fun steal(stealDto: StealDto) {
        if (stealDto.fromWho == currentPlayer.username) {
            println("Trying to steal from itself")
            return
        }
        if (stealDto.fromWho != null && !players.map { it.username }.contains(stealDto.fromWho)) {
            println("Username is not in this lobby")
            return
        }

        val knights = currentPlayer.cards.filter {
            !it.hasToWait && it.developmentTypes == Knight
        }

        if (stealDto.isKnight) {
            if (developmentPlayed) {
                return
            }
            if (knights.isEmpty()) {
                println("There is no knight to use")
                return
            }
        } else if (gameState != Seven) {
            return
        }

        val tile = map.tiles.find {
            it.id == stealDto.tileId
        } ?: return

        blockTile(tile)
        stealDto.fromWho?.let {
            stealFrom(it)
        }

        if (stealDto.isKnight) {
            currentPlayer.cards.remove(knights[0])
            currentPlayer.activeDevelopments.add(knights[0])
            knights[0].used = true
            calculateMostKnights()
        }
        gameState = Anything
        developmentPlayed = true
        sendAllPlayersResources()
    }

    private fun calculateMostKnights() {
        val currentHolder = players.find { it.ownerOfMostKnights }
        val max = players.maxOf { it.getKnights().count { it.used } }
        if (max < minimumForMostKnights) return
        val potentials = players.filter { it.getKnights().count { it.used } == max }
        if (potentials.size == 1) {
            if (potentials.single() != currentHolder) {
                currentHolder?.ownerOfMostKnights = false
                potentials.single().ownerOfMostKnights = true
            }
        }
    }
    //endregion

    //region Helpers

    fun calculatePoints() {
        val points = players.map {
            it to it.calculateMyPoints()
        }
        val potentialWinners = points.filter {
            it.second >= pointsToWin
        }
        if (potentialWinners.size == 1) {
            if (potentialWinners.map { it.first }.contains(currentPlayer)) {
                sendPlayers(
                    WinnersDto(potentialWinners.map { it.first.username })
                )
                potentialWinners.forEach {
                    winners.add(it.first)
                }
                gameState = Won
            }
        } else if (potentialWinners.size > 1) {
            val max = potentialWinners.maxOf { it.second }
            val maxPoints = potentialWinners.filter { it.second == max }
            if (maxPoints.map { it.first }.contains(currentPlayer)) {
                sendPlayers(
                    WinnersDto(maxPoints.map { it.first.username })
                )
                maxPoints.forEach {
                    winners.add(it.first)
                }
                gameState = Won
            }
        }
        if (gameState == Won) {
            println("Game is won!")
            winners.forEach {
                println("Is a winner: ${it.username}")
            }
        }
    }

    private fun sendRemainingDevelopmentCardCount() {
        val datas = players.map {
            it to DevelopmentCardsRemainingDto(developmentPool.remaining()).toJson()
        }
        sessionService.sendDatas(datas)
    }

    private fun sendAllPlayersResources() {
        val datas = players.map { player ->
            player to ResourcesDto(
                (players - player).map { it.toDto(true) } + player.toDto(false)
            ).toJson()
        }
        sessionService.sendDatas(datas)
        if (currentTradeOffers.isEmpty()) return
        val offers = currentTradeOffers.mapIndexed { index, playerTradeDto ->
            playerTradeDto.withWho to playerTradeDto.toOffer(
                index,
                currentPlayer.username
            )
        }
        val offerDats = players.map { player ->
            player to PlayerTradeOffersDto(
                offers.filter { it.first == player.username }.map { it.second }
            ).toJson()
        }
        sessionService.sendDatas(offerDats)
    }

    private fun stealFrom(username: String) {
        val player = players.find { it.username == username } ?: return
        val resources = mutableListOf<ResourceType>()
        player.resources.forEach { (resourceType, count) ->
            resources.addAll(Array(count) { resourceType })
        }
        val stolen = resources.random()
        currentPlayer.resources[stolen] = currentPlayer.resources[stolen]!! + 1
        player.resources[stolen] = player.resources[stolen]!! - 1
    }

    private fun blockTile(tile: Tile) {
        val previous = map.tiles.find {
            it.isBlocked
        } ?: return

        previous.isBlocked = false
        sendPlayers(ChangeOnBoardDto(previous.id, ChangeType.REMOVEBLOCK))
        tile.isBlocked = true
        sendPlayers(ChangeOnBoardDto(tile.id, ChangeType.ADDBLOCK))
    }

    private fun enableDevelopments() {
        currentPlayer.cards.forEach {
            it.hasToWait = false
        }
        sessionService.sendDatas(
            listOf(
                currentPlayer to ResourcesDto(listOf(currentPlayer.toDto())).toJson()
            )
        )
    }

    fun afterGameStatistics() {
        players.forEach {
            val user = databaseService.getUserByusername(it.username)
            user.single().gamesPlayed++
            if (winners.contains(it)) {
                user.single().gamesWon++
            }
            databaseService.updateUser(user.single())
        }
    }

    private fun rollTheDice() {
        val rolledDices = diceRoller.rollTheDice()

        val rolledNumber = rolledDices.first + rolledDices.second
        sessionService.sendDatas(
            players.map { it to DiceRollDto(rolledDices.first, rolledDices.second).toJson() }
        )
        if (rolledNumber == 7) {
            gameState = Seven
            sessionService.sendDatas(
                listOf(
                    currentPlayer to FromServer(FromServerPayloadType.SEVEN).toJson()
                )
            )
            return
        }

        val rolledTiles = map.tiles.filter {
            it.rolledNumber == rolledNumber
        }

        rolledTiles.forEach { tile ->
            tile.vertices.forEach {
                if (it?.owner != null && tile.type != DESERT) {
                    it.owner!!.resources[tile.type.toResourceType()!!] =
                        (it.owner!!.resources[tile.type.toResourceType()]
                            ?: 0) + if (it.buildingType == BuildType.SETTLEMENT) 1 else 2
                }
            }
        }

    }

    private fun checkForLongestRoad() {
        val longestRoads = players.associateWith { getLongestRoad(it) }
        val maxLength = longestRoads.maxOf { it.value }

        if (maxLength < minimumForLongestRoad) return
        val candidates = longestRoads.filter {
            it.value == maxLength
        }.map {
            it.key
        }
        if (candidates.contains(ownerOfLongestRoad)) return
        if (candidates.size > 1) return
        candidates.single().ownerOfLongestRoad = true
        ownerOfLongestRoad?.ownerOfLongestRoad = false
        ownerOfLongestRoad = candidates.single()
        sendAllPlayersResources()
    }

    private fun getLongestRoad(player: Player): Int {
        val vertexes = map.vertexes.filter {
            it.edges.find {
                it.owner == player
            } != null
        }
        if (vertexes.isEmpty()) return 0

        return vertexes.maxOf {
            println("db")
            longestRoadFrom(it, player)
        }
    }

    private fun longestRoadFrom(vertex: Vertex, player: Player): Int {
        fun recursion(v: Vertex, player: Player, discoveredVertexes: MutableSet<String>): MutableSet<String> {

            if (v.owner != null && v.owner != player) return discoveredVertexes

            val deepCopied = mutableSetOf<String>()
            deepCopied.addAll(discoveredVertexes)
            deepCopied.add(v.id)
            var longest = deepCopied
            v.edges.forEach {
                if (it.owner != null && it.owner == player) {
                    if (it.otherVertex(v).id !in discoveredVertexes) {
                        val newSet = recursion(it.otherVertex(v), player, deepCopied)
                        if (newSet.size > longest.size) {
                            longest = newSet
                        }
                    }

                }
            }
            return longest
        }
        return recursion(vertex, player, mutableSetOf()).size - 1
    }

    //endregion
}

enum class GameState {
    Anything,
    Seven,
    Won,
    Ended
}
