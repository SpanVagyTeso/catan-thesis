package game

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.game.MonopolyDto
import com.catan.sdk.dto.game.StealDto
import com.catan.sdk.dto.game.TwoRoadDto
import com.catan.sdk.dto.game.YearOfPlentyDto
import com.catan.sdk.dto.game.fromclient.BuyDto
import com.catan.sdk.dto.game.fromclient.BuyType.*
import com.catan.sdk.dto.game.fromclient.MaritimeTradeDto
import com.catan.sdk.dto.game.fromclient.PlaceBeginningDto
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
import service.DatabaseService
import service.SessionService

class Game(
    val players: List<Player>,
    private val sessionService: SessionService,
    private val databaseService: DatabaseService,
    private val diceRoller: DiceRoller = DiceRoller(),
    private val villagesAtBeginning: Int = 2,
    private val roadsAtBeginning: Int = 2,
    private val maxVillages: Int = 5,
    private val maxRoads: Int = 15,
    private val maxCities: Int = 5,
    private val minimumForLongestRoad: Int = 5,
    private val developmentPool: DevelopmentPool = DevelopmentPool(
        15,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        2
    ),
    private val pointsToWin: Int = 10,
    currentPlayerIndex: Int = 0
) {

    var currentPlayer: Player
    val map: Map = Map()
    private var beginningPlacementsRoad: MutableMap<String, Int>
    private val beginningPlacementsVillage: MutableMap<String, Int>
    var gameState = Anything
    var atBeginning = true
        private set

    var ownerOfLongestRoad: Player? = null
    var winners = mutableListOf<Player>()


    init {
        currentPlayer = players[currentPlayerIndex]

        map.generateTiles(5)
        map.attachAllTiles()
        beginningPlacementsRoad = players.associate {
            it.username to roadsAtBeginning
        } as MutableMap<String, Int>
        beginningPlacementsVillage = players.associate {
            it.username to villagesAtBeginning
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

            VILLAGE -> {
                if (dto.id == null) return
                buyVillage(dto.id!!)
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

        beginningPlacementsVillage.forEach {
            anyLeft = anyLeft || (it.value != 0)
        }

        checkLocations(dto.edgeId, dto.vertexId)
        val outcome = placeVillageAtBeginning(dto.vertexId) && placeRoadAtBeginning(dto.edgeId)
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
                ChangeType.VILLAGE,
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
        sendPlayers(CurrentPlayerDto(currentPlayer.username, !atBeginning))
        rollTheDice()
        enableDevelopments()
        sendAllPlayersResources()
        sendRemainingDevelopmentCardCount()
    }

    fun playerLeft(username: String){
        val player = players.find {
            it.username == username
        } ?: return
        gameState = Ended
        sendPlayers(
            FromServer(SOMEONELEFT)
        )
    }

    fun maritimeTrade(tradeDto: MaritimeTradeDto) {
        if(tradeDto.fromResource == tradeDto.toResource) return
        when(tradeDto.tradeType) {
            FourToOne -> fourToOneTrade(tradeDto)
            ThreeToOne -> threeToOneTrade(tradeDto)
            else -> twoToOne(tradeDto)
        }
        sendAllPlayersResources()
    }

    //region Maritime Trade stuff
    private fun fourToOneTrade(tradeDto: MaritimeTradeDto){
        if(currentPlayer.resources[tradeDto.fromResource]!! < 4) return
        currentPlayer.resources[tradeDto.fromResource] = currentPlayer.resources[tradeDto.fromResource]!! - 4
        currentPlayer.resources[tradeDto.toResource] = currentPlayer.resources[tradeDto.toResource]!! + 1

    }

    private fun threeToOneTrade(tradeDto: MaritimeTradeDto){
        if(currentPlayer.resources[tradeDto.fromResource]!! < 3) return
        currentPlayer.resources[tradeDto.fromResource] = currentPlayer.resources[tradeDto.fromResource]!! - 3
        currentPlayer.resources[tradeDto.toResource] = currentPlayer.resources[tradeDto.toResource]!! + 1

    }

    private fun twoToOne(tradeDto: MaritimeTradeDto) {
        if(currentPlayer.resources[tradeDto.fromResource]!! < 2) return
        val goodTrade = when(tradeDto.tradeType){
            TwoToOneWood -> tradeDto.fromResource == Lumber || tradeDto.toResource == Lumber
            TwoToOneSheep -> tradeDto.fromResource == Wool || tradeDto.toResource == Wool
            TwoToOneBrick -> tradeDto.fromResource == Brick || tradeDto.toResource == Brick
            TwoToOneOre -> tradeDto.fromResource == Ore || tradeDto.toResource == Ore
            TwoToOneWheat -> tradeDto.fromResource == Grain || tradeDto.toResource == Grain
            else -> false
        }
        if(!goodTrade) return
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
                cardType == Knight
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

    private fun buyVillage(id: String) {
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

        if (!placeVillage(vertex)) return

        currentPlayer.resources[Lumber] = currentPlayer.resources[Lumber]!! - 1
        currentPlayer.resources[Brick] = currentPlayer.resources[Brick]!! - 1
        currentPlayer.resources[Wool] = currentPlayer.resources[Wool]!! - 1
        currentPlayer.resources[Grain] = currentPlayer.resources[Grain]!! - 1

        checkForLongestRoad()
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                vertex.id,
                ChangeType.VILLAGE,
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

    private fun placeVillageAtBeginning(id: String): Boolean {
        if (beginningPlacementsVillage[currentPlayer.username]!! < 1) return false
        val tile = map.tiles.find {
            it.vertices.find {
                it?.id == id
            } != null
        } ?: return false
        val vertex = tile.vertices.find {
            it?.id == id
        } ?: return false

        if (!placeVillage(vertex)) return false
        beginningPlacementsVillage[currentPlayer.username] = beginningPlacementsVillage[currentPlayer.username]!! - 1
        println("Placing village at beginning for ${currentPlayer.username} remaining ${beginningPlacementsVillage[currentPlayer.username]}")
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
    private fun placeVillage(vertex: Vertex): Boolean {
        if (vertex.owner != null) return false
        if (currentPlayer.buildings[BuildType.VILLAGE]!! == maxVillages) return false
        vertex.owner = currentPlayer
        vertex.buildingType = BuildType.VILLAGE
        currentPlayer.buildings[BuildType.VILLAGE] = currentPlayer.buildings[BuildType.VILLAGE]!! + 1
        return true
    }

    private fun placeCity(vertex: Vertex): Boolean {
        if (vertex.owner != currentPlayer && vertex.buildingType != BuildType.VILLAGE) return false
        if (currentPlayer.buildings[BuildType.CITY]!! == maxCities) return false
        vertex.owner = currentPlayer
        vertex.buildingType = BuildType.CITY
        currentPlayer.buildings[BuildType.VILLAGE] = currentPlayer.buildings[BuildType.VILLAGE]!! - 1
        currentPlayer.buildings[BuildType.CITY] = currentPlayer.buildings[BuildType.CITY]!! + 1
        return true
    }

    private fun checkIfRoadCanBePlaced(edge: Edge, pseudoEdge: Edge? = null) = listOf(
        edge.endPoints.first,
        edge.endPoints.second
    ).any {
        it.owner == currentPlayer && it.edges.any { it.owner == currentPlayer || it === pseudoEdge }
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
        val resourceType = monopolyDto.resourceType
        var allOfTheResources = 0
        players.forEach {
            if (it != currentPlayer) {
                allOfTheResources += it.resources[resourceType]!!
                it.resources[resourceType] = 0
            }
        }
        currentPlayer.resources[resourceType] = currentPlayer.resources[resourceType]!! + allOfTheResources
        sendAllPlayersResources()
    }

    fun useYearOfPlenty(yearOfPlentyDto: YearOfPlentyDto) {
        currentPlayer.resources[yearOfPlentyDto.resource1] = currentPlayer.resources[yearOfPlentyDto.resource1]!! + 1
        currentPlayer.resources[yearOfPlentyDto.resource2] = currentPlayer.resources[yearOfPlentyDto.resource2]!! + 1
        sendAllPlayersResources()
    }

    fun useTwoRoad(twoRoadDto: TwoRoadDto) {
        if (currentPlayer.buildings[BuildType.ROAD]!! == maxRoads) {
            println("No roads remaining")
            return
        } else if (currentPlayer.buildings[BuildType.ROAD]!! + 1 == maxRoads && twoRoadDto.edgeId2 != null) {
            println("No roads remaining")
            return
        }
        if (currentPlayer.getRoadBuildings().count { !it.hasToWait && !it.used } < 1) {
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
        sessionService.sendDatas(players.map {
            it to ChangeOnBoardDto(
                edge1.id,
                ChangeType.ROAD,
                currentPlayer.username
            ).toJson()
        })
        if (edge2 != null) {
            sessionService.sendDatas(players.map {
                it to ChangeOnBoardDto(
                    edge2.id,
                    ChangeType.ROAD,
                    currentPlayer.username
                ).toJson()
            })
        }
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
            calculateMostKnights()
        }
        gameState = Anything
        sendAllPlayersResources()
    }

    private fun usePoint(devType: DevelopmentTypes) {
        val card = currentPlayer.cards.find {
            it.developmentTypes == devType
        } ?: return
        card.used = true
        currentPlayer.cards.remove(card)
        currentPlayer.activeDevelopments + card
    }

    private fun calculateMostKnights() {
        val currentHolder = players.find { it.ownerOfMostKnights }
        val max = players.maxOf { it.getKnights().count { it.used } }
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
                            ?: 0) + if (it.buildingType == BuildType.VILLAGE) 1 else 2
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
