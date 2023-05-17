package com.catan.sdk.entities

import java.rmi.UnexpectedException


class Edge(val endPoints: Pair<Vertex, Vertex>, val id: String, val canBeOwned: Boolean) {
    var owner: Player? = null

    fun containVertex(vertex: Vertex): Boolean = endPoints.first == vertex || endPoints.second == vertex

    fun otherVertex(vertex: Vertex) = when(vertex){
        endPoints.first -> endPoints.second
        endPoints.second ->endPoints.first
        else -> throw UnexpectedException("Vertex is not contained by this edge")
    }
}
