package com.catan.sdk.entities

import com.catan.sdk.entities.TradeType.FourToOne


class Vertex(
    val id: String
) {
    val rolledNumbers = mutableListOf<Int>()
    var owner: Player? = null
    var buildingType: BuildType? = null
    var tradeType = FourToOne


    val edges: MutableList<Edge> = mutableListOf()

    fun addEdge(vertex: Vertex, outEdges: MutableSet<Edge>, canBeOwned: Boolean = true) {
        println("connection $id and ${vertex.id}")
        if (vertex == this) return
        if (edges.any { it.containVertex(vertex) }) return

        val edge = Edge(
            this to vertex, "E${outEdges.size}", canBeOwned
        )
        outEdges.add(edge)

        edges.add(edge)
        vertex.edges.add(edge)

    }

    fun canBeBoughtBy(player: Player, ignoreRoad: Boolean = true): Boolean {
        if (isThereAdjacentVillageOrCity()) return false
        if (ignoreRoad) return true
        return edges.find { it.owner == player } != null
    }

    private fun isThereAdjacentVillageOrCity() = edges.find {
        it.endPoints.second.owner != null || it.endPoints.first.owner != null
    } != null

    override fun toString() = """VERTEX
        | ID: $id
        | Rolled Number: $rolledNumbers
        | owner: ${owner?.username}
    """.trimMargin()
}

data class MaritimeLocations(
    val id: String,
    val type: TradeType
)

