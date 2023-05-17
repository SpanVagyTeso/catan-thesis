package com.catan.sdk.entities

class Tile {
    val vertices = mutableListOf<Vertex?>()

    init {
        for (i in 0..5) {
            vertices.add(null)
        }
    }

    var type: FieldType
    var rolledNumber: Int
    val id: String
    var EDGE_COUNTER: Int
    var isBlocked = false

    constructor(
        type: FieldType,
        rolledNumber: Int,
        id: String,
        edgeCounter: Int
    ) {
        this.type = type
        this.id = id
        this.rolledNumber = rolledNumber
        this.EDGE_COUNTER = edgeCounter
    }

    constructor(
        tileDto: TileDto
    ) {
        id = tileDto.id
        type = tileDto.type
        rolledNumber = tileDto.rolledNumber
        EDGE_COUNTER = 0
        isBlocked = tileDto.isBlocked
    }

    fun vertexOwnedBy(username: String) = vertices.map {
        it?.id
    }.contains(username)


    fun connectVertices(edges: MutableSet<Edge>) {
        for (i in 0..vertices.size - 2) {
            vertices[i]!!.addEdge(vertices[i + 1]!!, edges)
        }
        vertices[vertices.size - 1]!!.addEdge(vertices[0]!!, edges)
    }

    fun addRolledNumberToVertices() {
        vertices.forEach {
            it?.let {
                it.rolledNumbers.add(rolledNumber)
                println("Adding $rolledNumber to ${it.id}")
            }
        }
    }

    fun verticesToString(): String {
        var str = ""
        for (i in 0 until vertices.size) {
            str += "id: $i\n"
            vertices[i]?.let {
                str += it.toString().replace(oldValue = "\n", newValue = "\n    ")
            }
            str += "\n"
        }
        return str
    }

    override fun toString(): String {
        return """
        TILE
            Id: $id
            Type: $type
            Rolled Number: $rolledNumber
        """.trimIndent()
    }
}
