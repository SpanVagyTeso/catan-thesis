package gui.views

import com.catan.sdk.entities.*
import com.catan.sdk.entities.BuildType.CITY
import com.catan.sdk.entities.BuildType.SETTLEMENT
import com.catan.sdk.entities.DevelopmentTypes.*
import com.catan.sdk.entities.TradeType.*
import controller.GameState
import controller.GameState.*
import gui.custom.*
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.effect.BoxBlur
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Color.*
import tornadofx.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

private const val TILE_HEIGHT = 60.0

class GameView : BaseView() {
    val gameController = controller.gameController

    val mapHud = stackpane()
    val otherPlayerHud = gridpane()
    val playerHud = gridpane()
    val miscellaneousHud = hbox()

    val playField = borderpane {
        bottom = playerHud
        center = mapHud
        right = otherPlayerHud
        top = miscellaneousHud

        onKeyPressed = EventHandler {
            if (it.code == KeyCode.ESCAPE) {
                if (gameController.state !in setOf(OtherPlayer, StartOther, Start, StartPlaceRoad)) {
                    setGameState(Normal)
                    closeMenu()
                    refresh()
                }
            }
        }
        onMouseClicked = EventHandler {
            if (it.button != MouseButton.PRIMARY) return@EventHandler
            val id = it.pickResult.intersectedNode.id
            if (id != null) {
                println("Clicked on id $id")
                handleClick(id)
            }
        }
        paddingRight = 5
    }


    val blur: BoxBlur = BoxBlur()

    val popUp = stackpane()

    init {
        gameController.refreshView = { refresh() }
        gameController.showWinners = { showWinners() }
        gameController.gameAbrubtlyEnded = { gameAbrubtlyEnds() }
        refresh()
    }

    override fun refresh() {
        runLater {
            refreshMapHud()
            refreshPlayerHud()
            refreshOtherPlayerHud()
            refreshMiscellaneousHud()
        }
    }

    private fun refreshMiscellaneousHud() {
        miscellaneousHud.clear()
        miscellaneousHud.apply {
            spacing = 5.0
            label {
                text = currentActionText()
            }
            if (gameController.state !in setOf(Start, StartOther, StartPlaceRoad)) {
                label {
                    text = "Rolled dice: ${gameController.dices.first}, ${gameController.dices.second}"
                }
            }
        }
    }

    private fun currentActionText(): String {
        if (gameController.currentPlayer != gameController.me) {
            return "${gameController.currentPlayer?.username}'s turn"
        }

        return when (gameController.state) {
            UseKnight -> {
                "Use your knight please!"
            }

            UseRoads -> {
                "Place down your road(s)!"
            }

            UseYearsOfPlenty -> {
                "Choose your two resources!"
            }

            UseMonopoly -> {
                "Choose a resource to monopolize!"
            }

            Start -> {
                "Place down a settlement!"
            }

            StartPlaceRoad -> {
                "Place down a road next to the previous settlement!"
            }

            Seven -> {
                "Please move the thief"
            }

            OtherPlayer -> {
                "Other player's turn"
            }

            else -> {
                "Your turn"
            }
        }
    }

    private fun refreshMapHud() {
        mapHud.clear()
        mapHud.apply {
            group {
                drawHexagons(controller.map())

                drawEdges(gameController.map.edges.filter { it.owner != null })
                drawSettlementsAndCities(corners)


                when (gameController.state) {
                    Start -> {
                        if (gameController.currentPlayer == gameController.me)
                            drawCirclesOnCorner(gameController.getGoodCorners(true))
                    }

                    StartPlaceRoad -> {
                        drawEdges(gameController.getRoadPlacementForBeginning(), RED, 6.0)
                    }

                    BuyRoad, UseRoads -> {
                        drawEdges(gameController.getGoodRoads(), RED, 6.0)
                    }

                    BuySettlement -> {
                        drawCirclesOnCorner(gameController.getGoodCorners())
                    }

                    BuyCity -> {
                        val settlements = gameController.getCurrentPlayerSettlements().toSet()
                        highlightSettlements(corners.filter {
                            settlements.contains(it.key)
                        })
                    }

                    else -> {}
                }
            }
        }
    }

    private fun refreshPlayerHud() {
        playerHud.clear()
        playerHud.apply {
            add(
                PlayerUi(
                    gameController,
                    { developmentMenu() },
                    { buyMenu() },
                    { gameController.passTheTurn() },
                    { refresh() },
                    { maritimeTradeMenu() },
                    { playerTradeMenu() }
                )
            )
        }
    }

    private fun refreshOtherPlayerHud() {
        otherPlayerHud.clear()
        otherPlayerHud.apply {
            gameController.otherPlayers().forEach {
                hgap = 5.0

                row {
                    add(OtherPlayerCard(it.value))
                }
            }
        }
    }

    private fun handleClick(id: String) {
        when (gameController.state) {
            BuyCity -> {
                if (id.startsWith("V")) {
                    gameController.buyCity(id)
                }
                setGameState(Normal)
            }

            BuySettlement -> {
                if (id.startsWith("V")) {
                    gameController.buySettlement(id)
                }
                setGameState(Normal)
            }

            BuyRoad -> {
                if (id.startsWith("E")) {
                    gameController.buyEdge(id)
                }
                setGameState(Normal)
            }

            UseRoads -> {
                if (id.startsWith("E")) {
                    gameController.consumeTwoRoads(id)
                    refresh()
                }
            }

            UseKnight, Seven -> {
                if (id.startsWith("T")) {
                    val tile = gameController.map.tiles.find { it.id == id } ?: return
                    if (tile.isBlocked) return
                    val players = tile.vertices.filter {
                        it?.owner != null && it.owner != gameController.me
                    }.mapNotNull { it!!.owner }.toSet()
                    if (players.isEmpty()) {
                        gameController.steal(tile, gameController.state == UseKnight, null)
                        setGameState(Normal)
                    } else if (players.size == 1) {
                        gameController.steal(tile, gameController.state == UseKnight, players.single())
                        setGameState(Normal)
                    } else {
                        stealingMenu(players.toList(), tile, gameController.state == UseKnight)
                    }
                }
            }

            Start -> {
                if (id.startsWith("V")) {
                    gameController.setStartSettlement(id)
                    refresh()
                }
            }

            StartPlaceRoad -> {
                if (id.startsWith("E")) {
                    gameController.sendStartSettlementAndRoad(id)
                    refresh()
                }
            }

            else -> {}
        }
    }

    private fun Parent.drawHexagons(map: MutableList<MutableList<Tile>>) {
        val yOffSet = calculateYOffSetFromHeight(TILE_HEIGHT)
        val s = map.size
        var off = s / 2
        map.forEachIndexed { rowInd, row ->
            row.forEachIndexed { colInd, it ->
                hexagon(TILE_HEIGHT, it) {
                    offsetX = TILE_HEIGHT * 2 * colInd - TILE_HEIGHT * off
                    offsetY = yOffSet * rowInd
                }
            }
            if (rowInd >= s / 2) off--
            else off++
        }
    }

    private fun Parent.drawSettlementsAndCities(corners: HashMap<Vertex, Pair<Double, Double>>) {
        corners.forEach { (k, v) ->
            if (k.owner != null) {
                when (k.buildingType) {
                    SETTLEMENT -> {
                        settlement {
                            color = k.owner!!.playerColor.toJavaColor()
                            layoutX = v.first - boundsInLocal.maxX / 2
                            layoutY = v.second - boundsInLocal.maxY / 2
                            oId = k.id

                        }
                    }

                    CITY -> {
                        city {
                            color = k.owner!!.playerColor.toJavaColor()
                            layoutX = v.first - boundsInLocal.maxX / 2
                            layoutY = v.second - boundsInLocal.maxY / 2
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun Parent.highlightSettlements(corners: Map<Vertex, Pair<Double, Double>>) {
        corners.forEach { (k, v) ->
            if (k.owner != null) {
                when (k.buildingType) {
                    SETTLEMENT -> {
                        settlement {
                            isHighlighted = true
                            color = k.owner!!.playerColor.toJavaColor()
                            layoutX = v.first - boundsInLocal.maxX / 2
                            layoutY = v.second - boundsInLocal.maxY / 2
                            oId = k.id
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun Parent.drawEdges(edges: List<Edge>, overrideColor: Color? = null, width: Double = 3.0) {
        edges.forEach {
            line {
                val endpoint = it.endPoints
                startX = corners[endpoint.first]!!.first
                startY = corners[endpoint.first]!!.second
                endX = corners[endpoint.second]!!.first
                endY = corners[endpoint.second]!!.second
                strokeWidth = width
                id = it.id
                stroke = overrideColor ?: it.owner!!.playerColor.toJavaColor()
            }
        }
    }

    private fun Parent.drawCirclesOnCorner(goodCorners: List<Vertex>) {
        goodCorners.forEach { vertex ->
            corners[vertex]!!.let {
                if (vertex.owner == null)
                    circleText(TILE_HEIGHT / 5, vertex.id) {
                        setAllId(vertex.id)
                        centerX(it.first)
                        centerY(it.second)
                        circle.fill = RED
                        text.text = ""
                    }
            }
        }
    }


    override val root = stackpane {
        add(playField)
    }

    private fun calculateYOffSetFromHeight(height: Double) = sqrt(
        (height / cos(Math.PI / 60) * 2).pow(2.0) - (height.pow(2.0))
    )

    fun showWinners() {
        runLater {
            popUp.apply {
                rectangle {
                    width = root.width / 2
                    height = root.height / 2
                    fill = WHITE
                }
                group {
                    gridpane {
                        hgap = 10.0
                        row {
                            label {
                                text = "Game ended!"
                            }
                        }
                        row {
                            if (gameController.winners.size == 1) {
                                label {
                                    text = "${gameController.winners.single().username} won!"
                                }
                            } else {
                                var winners = gameController.winners.joinToString(", ") { it.username }
                                label {
                                    text = "$winners are the winners!"
                                }
                            }
                        }
                        row {
                            button {
                                text = "Back to Lobbies!"
                                action {
                                    closeMenu()
                                    runLater {
                                        controller.currentView.replaceWith<LobbySelectionView>()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            playField.effect = blur
            root.add(popUp)
        }
    }

    fun gameAbrubtlyEnds() {
        runLater {
            popUp.apply {
                rectangle {
                    width = root.width / 2
                    height = root.height / 2
                    fill = WHITE
                }
                group {
                    gridpane {
                        hgap = 10.0
                        row {
                            label {
                                text = "Game ended!"
                            }
                        }
                        row {
                            label {
                                text = "Game ended! Somone left!"
                            }
                        }
                        row {
                            button {
                                text = "Back to Lobbies!"
                                action {
                                    closeMenu()
                                    runLater {
                                        controller.currentView.replaceWith<LobbySelectionView>()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            playField.effect = blur
            root.add(popUp)
        }
    }

    fun developmentMenu() {
        setGameState(DevelopmentMenu)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            text = "Your developments"
                        }
                    }
                    row {
                        label {
                            text = "Knights"
                        }
                        label {
                            text = "Year of Plenty"
                        }
                        label {
                            text = "Road Building"
                        }
                        label {
                            text = "Monopoly"
                        }
                        label {
                            text = "1 Point cards"
                        }
                    }
                    row {
                        with(gameController.me!!) {
                            listOf(
                                getKnights(),
                                getYearOfPlenties(),
                                getRoadBuildings(),
                                getMonopolies()
                            ).forEach {
                                label {
                                    text = """ 
                                    Usable: ${it.count { !it.hasToWait && !it.used }}
                                    Has to wait: ${it.count { it.hasToWait && !it.used }}
                                    Used: ${it.count { it.used }}
                                """.trimIndent()
                                }
                            }
                            label {
                                var generatedText = ""
                                listOf(
                                    University,
                                    Market,
                                    Library,
                                    Chapel,
                                    GreatHall
                                ).forEach { type ->
                                    generatedText += "$type: ${getPointCards().count { it.developmentTypes == type }}\n"
                                }

                                text = generatedText
                            }
                        }
                    }
                    row {
                        with(gameController.me!!) {
                            button {
                                val knights = getKnights()
                                isDisable =
                                    knights.count { !it.hasToWait && !it.used } == 0 || gameController.developmentPlayed
                                text = "Use"
                                action {
                                    closeMenu()
                                    setGameState(UseKnight)
                                }
                            }
                            button {
                                val yearOfPlenties = getYearOfPlenties()
                                isDisable =
                                    yearOfPlenties.count { !it.hasToWait && !it.used } == 0 || gameController.developmentPlayed
                                text = "Use"
                                action {
                                    closeMenu()
                                    yearsOfPlentyMenu()

                                }
                            }
                            button {
                                val roads = getRoadBuildings()
                                isDisable =
                                    roads.count { !it.hasToWait && !it.used } == 0 || gameController.developmentPlayed
                                text = "Use"
                                action {
                                    setGameState(UseRoads)
                                    closeMenu()
                                }
                            }
                            button {
                                val monopolies = getMonopolies()
                                isDisable =
                                    monopolies.count { !it.hasToWait && !it.used } == 0 || gameController.developmentPlayed
                                text = "Use"
                                action {
                                    closeMenu()
                                    monopolyMenu()
                                }
                            }
                        }
                    }
                    row {
                        button {
                            text = "Close"
                            action {
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun stealingMenu(players: List<Player>, tile: Tile, isKnight: Boolean) {
        setGameState(Stealing)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            text = "Choose a player to steal from!"
                        }
                    }
                    players.forEach {
                        row {
                            add(OtherPlayerCard(it).apply {
                                onMouseClicked = EventHandler { event ->
                                    if (event.button != MouseButton.PRIMARY) return@EventHandler
                                    gameController.steal(tile, isKnight, it)
                                    closeMenu()
                                    setGameState(Normal)
                                }
                            })
                        }
                    }

                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun monopolyMenu() {
        setGameState(UseMonopoly)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            text = "Choose a resource to monopolize!"
                        }
                    }
                    val resourceBox = combobox<ResourceType> {
                        items = observableListOf(
                            *ResourceType.values()
                        )
                    }
                    row {
                        add(resourceBox)
                    }
                    row {
                        button {
                            text = "Buy"
                            action {
                                closeMenu()
                                setGameState(Normal)
                                gameController.useMonopoly(resourceBox.value)
                            }
                        }
                        button {
                            text = "Close"
                            action {
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun yearsOfPlentyMenu() {
        setGameState(UseYearsOfPlenty)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            text = "Choose two resources!"
                        }
                    }
                    val resourceBox1 = combobox<ResourceType> {
                        items = observableListOf(
                            *ResourceType.values()
                        )
                    }
                    val resourceBox2 = combobox<ResourceType> {
                        items = observableListOf(
                            *ResourceType.values()
                        )
                    }
                    row {
                        add(resourceBox1)
                        add(resourceBox2)
                    }
                    row {
                        button {
                            text = "Buy"
                            action {
                                closeMenu()
                                setGameState(Normal)
                                gameController.useYearsOfPlenty(resourceBox1.value, resourceBox2.value)
                            }
                        }

                        button {
                            text = "Close"
                            action {
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun buyMenu() {
        setGameState(BuyMenu)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            val resourceText = gameController.me!!.resources.map {
                                "${it.key}: ${it.value}"
                            }.joinToString(", ")
                            text = "Resources: $resourceText"
                        }
                    }
                    row {
                        gridpane {
                            vgap = 5.0
                            hgap = 5.0
                            row {
                                label {
                                    text = "Road"
                                }
                                label {
                                    text = "Settlement"
                                }
                                label {
                                    text = "City"
                                }
                                label {
                                    text = "Development"
                                }
                            }
                            row {
                                val myColor = gameController.me!!.playerColor.toJavaColor()
                                rectangle {
                                    width = 10.0
                                    height = 10.0
                                    fill = myColor
                                }
                                settlement {
                                    size = 25.0
                                    color = myColor
                                }
                                city {
                                    size = 25.0
                                    color = myColor
                                }
                                vbox {
                                    label {
                                        text = "Remaining:"
                                    }
                                    label {
                                        text = gameController.remainingDevelopmentCards.toString()
                                    }
                                }
                            }
                            row {
                                button {
                                    text = "Buy"
                                    isDisable = !gameController.canBuyRoad()
                                    action {
                                        closeMenu()
                                        buyRoad()
                                    }
                                }
                                button {
                                    text = "Buy"
                                    isDisable = !gameController.canBuySettlement()
                                    action {
                                        closeMenu()
                                        buySettlement()
                                    }
                                }
                                button {
                                    text = "Buy"
                                    isDisable = !gameController.canBuyCity()
                                    action {
                                        closeMenu()
                                        buyCity()
                                    }
                                }
                                button {
                                    text = "Buy"
                                    isDisable = !gameController.canBuyDevelopment()
                                    action {
                                        closeMenu()
                                        buyUpgrade()
                                    }
                                }
                            }
                        }
                    }
                    row {
                        button {
                            text = "Close"
                            action {
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun maritimeTradeMenu() {
        setGameState(MaritimeTradeMenu)
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                gridpane {
                    hgap = 10.0
                    row {
                        label {
                            val resourceText = gameController.me!!.resources.map {
                                "${it.key}: ${it.value}"
                            }.joinToString(", ")
                            text = "Resources: $resourceText"
                        }
                    }
                    val resourceBox1 = combobox<ResourceType> {
                        items = observableListOf(
                            *ResourceType.values()
                        )
                    }
                    val resourceBox2 = combobox<ResourceType> {
                        items = observableListOf(
                            *ResourceType.values()
                        )
                    }
                    row {
                        hgap = 5.0

                        row {
                            add(resourceBox1)
                            add(resourceBox2)
                        }
                    }

                    row {
                        button {
                            text = "Four to One"
                            action {
                                gameController.maritimeTrade(
                                    resourceBox1.value, resourceBox2.value, FourToOne
                                )
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                    row {
                        button {
                            text = "Close"
                            action {
                                closeMenu()
                                setGameState(Normal)
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun playerTradeMenu() {
        val resourceBox1 = combobox {
            items = observableListOf(
                *ResourceType.values()
            )
        }
        val resourceBox2 = combobox {
            items = observableListOf(
                *ResourceType.values()
            )
        }
        val playerSelector = combobox {
            items = observableListOf(
                gameController.players.keys.filter { it != gameController.me!!.username }
            )
        }
        val textField1 = textfield { }
        val textField2 = textfield { }
        popUp.apply {
            rectangle {
                width = root.width / 2
                height = root.height / 2
                fill = WHITE
            }
            group {
                borderpane {
                    top  {
                        label {
                            val resourceText = gameController.me!!.resources.map {
                                "${it.key}: ${it.value}"
                            }.joinToString(", ")
                            text = "Resources: $resourceText"
                        }
                    }
                    if(gameController.state == Normal) {
                        center = gridpane {
                            hgap = 10.0

                            row {
                                vgap = 5.0
                                label {
                                    text = "For my"
                                }
                                add(textField1)
                                add(resourceBox1)
                            }
                            row {
                                vgap = 5.0
                                label {
                                    text = "To their"
                                }
                                add(textField2)
                                add(resourceBox2)
                                add(playerSelector)
                            }
                            row {
                                button {
                                    text = "Send"
                                    action {
                                        try {
                                            gameController.offerTrade(
                                                resourceBox1.value,
                                                textField1.text.toInt(),
                                                resourceBox2.value,
                                                textField2.text.toInt(),
                                                playerSelector.value
                                            )
                                        } catch (_: Exception) {

                                        }
                                        closeMenu()
                                        setGameState(Normal)

                                    }
                                }
                            }
                        }
                    } else {
                        left = vbox {
                            gameController.offers.forEach {
                                add(TradeOffer(
                                    it,
                                ) {
                                    gameController.acceptTrade(it.tradeId)
                                    closeMenu()
                                })
                            }
                        }
                    }
                    bottom {
                        button {
                            text = "Close"
                            action {
                                closeMenu()
                            }
                        }
                    }
                }
            }
        }
        playField.effect = blur
        root.add(popUp)
    }

    fun closeMenu() {
        playField.effect = null
        root.clear()
        root.add(playField)

    }

    fun buyRoad() {
        setGameState(BuyRoad)
    }

    fun buySettlement() {
        setGameState(BuySettlement)
    }

    fun buyCity() {
        setGameState(BuyCity)
    }

    fun buyUpgrade() {
        gameController.buyUpgrade()
        setGameState(Normal)
    }

    private fun setGameState(state: GameState) {
        gameController.state = state
        refresh()
    }
}

fun TradeType.toResourceName(): String = when (this) {
    TwoToOneWood -> "Lumber"
    TwoToOneSheep -> "Wool"
    TwoToOneBrick -> "Brick"
    TwoToOneOre -> "Ore"
    TwoToOneWheat -> "Grain"
    else -> "any"
}

fun PlayerColor.toJavaColor(): Color = when (this) {
    PlayerColor.RED -> DARKRED
    PlayerColor.GREEN -> GREEN
    PlayerColor.BLUE -> BLUE
    PlayerColor.YELLOW -> WHITE
}
