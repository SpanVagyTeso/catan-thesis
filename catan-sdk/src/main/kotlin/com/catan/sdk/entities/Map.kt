package com.catan.sdk.entities

import com.catan.sdk.entities.FieldType.*
import com.catan.sdk.entities.TradeType.FourToOne

class Map {
    val rows = mutableListOf<MutableList<Tile>>()
    val tiles = mutableListOf<Tile>()
    val edges = mutableSetOf<Edge>()
    val vertexes = mutableSetOf<Vertex>()

    private var VERTEX_COUNTER = 0
    private var EDGE_COUNTER = 0
    private var TILE_COUNTER = 0

    fun loadFromDto(mapDto: MapDto) {
        rows.clear()
        tiles.clear()
        vertexes.clear()
        edges.clear()
        VERTEX_COUNTER = 0
        EDGE_COUNTER = 0
        TILE_COUNTER = 0
        mapDto.rows.forEach {
            val row = mutableListOf<Tile>()
            it.tiles.forEach {
                val t = Tile(it)
                row.add(t)
                tiles.add(t)
            }
            rows.add(row)
        }
    }

    fun tile(id: String) = tiles.filter {
        it.id == id
    }

    fun getMaritimeLocations() = vertexes.filter {
        it.tradeType != FourToOne
    }.map {
        MaritimeLocations(it.id, it.tradeType)
    }

    fun getBuyableVertexes(player: Player, ignoreRoad: Boolean = false): List<Vertex> = vertexes.filter {
        it.canBeBoughtBy(player, ignoreRoad)
    }


    fun getBuyableEdges(player: Player): List<Edge> = edges.filter {
        it.owner == null && it.canBeOwned && (it.endPoints.first.edges.find { it.owner == player } != null || it.endPoints.second.edges.find { it.owner == player } != null)
    }

    //Default
    fun generateTiles(
        numberOfRows: Int = 5,
        isGeneric: Boolean = true
    ) {
        rows.clear()
        tiles.clear()
        vertexes.clear()
        EDGE_COUNTER = 0

        var additive = true
        var numberOfTiles = 3
        for (i in 0 until numberOfRows) {
            val row = mutableListOf<Tile>()
            for (j in 0 until numberOfTiles) {
                val tile = Tile(
                    DESERT,
                    0,
                    "T${TILE_COUNTER++}",
                    EDGE_COUNTER
                )
                row.add(
                    tile
                )
                tiles.add(tile)
            }
            if (i >= (numberOfRows / 2)) additive = false
            numberOfTiles += if (additive) 1 else -1
            rows.add(row)
        }
        rows.forEach {
            println(it.size)
        }
        if (isGeneric) {
            setMapToGeneric()
        }
    }

    private fun setMapToGeneric() {
        tiles[0].apply {
            type = PASTURE
            rolledNumber = 11
        }
        tiles[1].apply {
            type = FIELDS
            rolledNumber = 6
        }
        tiles[2].apply {
            type = HILLS
            rolledNumber = 5
        }
        tiles[3].apply {
            type = PASTURE
            rolledNumber = 5
        }
        tiles[4].apply {
            type = FIELDS
            rolledNumber = 4
        }
        tiles[5].apply {
            type = MOUNTAINS
            rolledNumber = 3
        }
        tiles[6].apply {
            type = FOREST
            rolledNumber = 8
        }
        tiles[7].apply {
            type = MOUNTAINS
            rolledNumber = 8
        }
        tiles[8].apply {
            type = FOREST
            rolledNumber = 3
        }
        tiles[9].apply {
            type = DESERT
            rolledNumber = 0
            isBlocked = true
        }
        tiles[10].apply {
            type = FOREST
            rolledNumber = 11
        }
        tiles[11].apply {
            type = FIELDS
            rolledNumber = 9
        }
        tiles[12].apply {
            type = HILLS
            rolledNumber = 10
        }
        tiles[13].apply {
            type = PASTURE
            rolledNumber = 4
        }
        tiles[14].apply {
            type = HILLS
            rolledNumber = 6
        }
        tiles[15].apply {
            type = FIELDS
            rolledNumber = 12
        }
        tiles[16].apply {
            type = FOREST
            rolledNumber = 9
        }
        tiles[17].apply {
            type = PASTURE
            rolledNumber = 2
        }
        tiles[18].apply {
            type = MOUNTAINS
            rolledNumber = 10
        }

    }

    private fun attachTileToSide(
        attachingThis: Tile,
        bottomRight: Tile? = null,
        right: Tile? = null,
        topRight: Tile? = null,
        topLeft: Tile? = null,
        left: Tile? = null,
        bottomLeft: Tile? = null
    ) {
        val notAttached = mutableSetOf(0, 1, 2, 3, 4, 5)

        bottomRight?.let {
            with(attachingThis.vertices) {
                bottomRight.vertices[4]?.let {
                    this[0] = it
                    notAttached.remove(0)
                }

                bottomRight.vertices[3]?.let {
                    this[1] = it
                    notAttached.remove(1)
                }
            }
        }

        right?.let {
            with(attachingThis.vertices) {
                right.vertices[5]?.let {
                    this[1] = it
                    notAttached.remove(1)
                }

                right.vertices[4]?.let {
                    this[2] = it
                    notAttached.remove(2)
                }
            }
        }

        topRight?.let {
            with(attachingThis.vertices) {
                topRight.vertices[5]?.let {
                    this[3] = it
                    notAttached.remove(3)
                }

                topRight.vertices[0]?.let {
                    this[2] = it
                    notAttached.remove(2)
                }
            }
        }

        topLeft?.let {
            with(attachingThis.vertices) {
                topLeft.vertices[0]?.let {
                    this[4] = it
                    notAttached.remove(4)
                }

                topLeft.vertices[1]?.let {
                    this[3] = it
                    notAttached.remove(3)
                }
            }
        }

        left?.let {
            with(attachingThis.vertices) {
                left.vertices[2]?.let {
                    this[4] = it
                    notAttached.remove(4)
                }

                left.vertices[1]?.let {
                    this[5] = it
                    notAttached.remove(5)
                }
            }
        }

        bottomLeft?.let {
            with(attachingThis.vertices) {
                bottomLeft.vertices[3]?.let {
                    this[5] = it
                    notAttached.remove(5)
                }

                bottomLeft.vertices[2]?.let {
                    this[0] = it
                    notAttached.remove(0)
                }
            }
        }

        notAttached.forEach {
            val v = Vertex("V$VERTEX_COUNTER")
            VERTEX_COUNTER++
            vertexes.add(v)
            attachingThis.vertices[it] = v
            vertexes.add(attachingThis.vertices[it]!!)
        }
    }

    fun attachAllTiles() {
        for (i in 0 until tiles.size) {
            val tile = tiles[i]
            val cord = idToCordinate(i)
            attachTileToSide(
                tile,
                bottomLeft = getCoordBottomLeft(cord).toTile(),
                bottomRight = getCoordBottomRight(cord).toTile(),
                right = getCoordRight(cord).toTile()
            )
        }
        addEdges()
        addRolledNumberToVertices()
    }

    private fun addRolledNumberToVertices() {
        tiles.forEach {
            it.addRolledNumberToVertices()
        }
    }

    private fun addEdges() {
        tiles.forEach {
            println("Adding edges in tile: ${it.id}")
            it.connectVertices(edges)
        }
    }

    fun toDto() = MapDto(
        rows.map {
            RowDto(
                it.map { tile ->
                    tile.toDto()
                }
            )
        }
    )

    data class Coordinate(var row: Int, var column: Int) {

        constructor(p: Pair<Int, Int>) : this(p.first, p.second)

        fun toId(): Int? {
            if (row < 0) return null

            if (column < 0) return null

            if (row == 0) {
                return if (column < 3) column else null
            }
            if (row == 1) {
                return if (column < 4) column + 3 else null
            }
            if (row == 2) {
                return if (column < 5) column + 7 else null
            }
            if (row == 3) {
                return if (column < 4) column + 12 else null
            }
            if (row == 4) {
                return if (column < 3) column + 16 else null
            } else return null
        }
    }

    fun Coordinate.toTile(): Tile? {
        val cord = toId()
        return cord?.let {
            tiles[it]
        }
    }

    private fun getCoordBottomRight(cord: Coordinate) = when (cord.row) {
        0 -> {
            Coordinate(row = -1, column = -1)
        }

        1 -> {
            Coordinate(row = 0, column = cord.column - 1)
        }

        2 -> {
            Coordinate(row = 1, column = cord.column - 1)
        }

        3 -> {
            Coordinate(row = 2, column = cord.column)
        }

        4 -> {
            Coordinate(row = 3, column = cord.column)
        }

        else -> {
            Coordinate(-1 to -1)
        }
    }

    private fun getCoordBottomLeft(cord: Coordinate): Coordinate {
        val bottomRight = getCoordBottomRight(cord)
        return Coordinate(row = bottomRight.row, column = bottomRight.column + 1)
    }

    private fun getCoordRight(cord: Coordinate) = Coordinate(row = cord.row, column = cord.column - 1)

    private fun idToCordinate(id: Int): Coordinate {
        if (id < 3) return Coordinate(row = 0, column = id)
        if (id < 7) return Coordinate(row = 1, column = id - 3)
        if (id < 12) return Coordinate(row = 2, column = id - 7)
        if (id < 16) return Coordinate(row = 3, column = id - 12)
        if (id < 19) return Coordinate(row = 4, column = id - 16)
        return Coordinate(-1 to -1)
    }


}

fun Tile.toDto() = TileDto(
    this.id,
    this.type,
    this.rolledNumber,
    this.isBlocked
)

class MapDto(
    val rows: List<RowDto>
)

class RowDto(
    val tiles: List<TileDto>
)

class TileDto(
    val id: String,
    val type: FieldType,
    val rolledNumber: Int,
    val isBlocked: Boolean
)


