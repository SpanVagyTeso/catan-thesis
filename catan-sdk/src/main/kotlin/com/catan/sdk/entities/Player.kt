package com.catan.sdk.entities

import com.catan.sdk.entities.BuildType.CITY
import com.catan.sdk.entities.BuildType.SETTLEMENT
import com.catan.sdk.entities.DevelopmentTypes.*


class Player {
    val username: String

    val resources: MutableMap<ResourceType, Int>
    val buildings = BuildType.values().associateWith { 0 } as MutableMap
    val cards = mutableListOf<DevelopmentCard>()
    val activeDevelopments = mutableListOf<DevelopmentCard>()
    var hiddenResources = 0
    var hiddenDevelopments = 0
    var playerColor: PlayerColor
    var ownerOfMostKnights: Boolean = false
    var ownerOfLongestRoad: Boolean = false

    constructor(username: String, playerColor: PlayerColor) {
        this.username = username
        this.playerColor = playerColor
        resources = ResourceType.values().associateWith { 0 } as MutableMap
    }

    constructor(dto: PlayerDto) {
        resources = ResourceType.values().associateWith { 0 } as MutableMap
        username = dto.userName
        playerColor = dto.playerColor
        dto.activeCards.forEach {
            activeDevelopments.add(it)
        }
        dto.hiddenResources?.let {
            hiddenResources = it
        }
        dto.hiddenDevelopments?.let {
            hiddenDevelopments = it
        }
        dto.resources?.let {
            it.forEach {
                resources[it] = (resources[it] ?: 0) + 1
            }
        }
        dto.cards?.let {
            it.forEach {
                cards.add(it)
            }
        }
    }

    fun refreshFromDto(dto: PlayerDto) {
        check(dto.userName == username) {
            "Wrong player"
        }
        dto.resources?.let {
            resources.keys.forEach {
                resources[it] = 0
            }

            it.forEach {
                resources[it] = (resources[it] ?: 0) + 1
            }
        }
        ownerOfLongestRoad = dto.ownerOfLongestRoad
        ownerOfMostKnights = dto.ownerOfMostKnights
        activeDevelopments.clear()
        dto.activeCards.forEach {
            activeDevelopments.add(it)
        }
        dto.cards?.let {
            cards.clear()
            it.forEach {
                cards.add(it)
            }
        }
        dto.hiddenResources?.let {
            hiddenResources = it
        }
        dto.hiddenDevelopments?.let {
            hiddenDevelopments = it
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false
        if (this.hashCode() != other.hashCode()) return false
        if (this.username != other.username) return false
        if (this.resources != other.resources) return false
        return this.resources == other.resources
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + resources.hashCode() + buildings.hashCode()
        return result
    }

    fun sizeOfResources() =
        resources.map {
            it.value
        }.sum()

    fun resourceList(): List<ResourceType> {
        val resourceList = mutableListOf<ResourceType>()
        resources.forEach { (resource, size) ->
            resourceList += Array(size) {
                resource
            }
        }
        return resourceList
    }

    fun getKnights() = (cards+activeDevelopments).filter { it.developmentTypes == Knight }

    fun getYearOfPlenties() = (cards+activeDevelopments).filter { it.developmentTypes == YearOfPlenty }

    fun getRoadBuildings() = (cards+activeDevelopments).filter { it.developmentTypes == RoadBuilding }

    fun getMonopolies() = (cards+activeDevelopments).filter { it.developmentTypes == Monopoly }

    fun calculateMyPoints(): Int {
        var sum = 0
        sum += buildings[SETTLEMENT] ?: 0
        sum += (buildings[CITY] ?: 0) * 2
        sum += getPointCards().count()
        sum += if (ownerOfLongestRoad) 2 else 0
        sum += if (ownerOfMostKnights) 2 else 0
        return sum
    }

    fun getPointCards() = (cards+activeDevelopments).filter {
        it.developmentTypes in setOf(
            University,
            Market,
            Library,
            Chapel,
            GreatHall
        )
    }

    fun toDto(hidden: Boolean = false): PlayerDto {
        return if (hidden) {
            PlayerDto(
                username,
                activeDevelopments,
                ownerOfMostKnights,
                ownerOfLongestRoad,
                playerColor,
                hiddenResources = sizeOfResources(),
                hiddenDevelopments = cards.size
            )
        } else {
            PlayerDto(
                username,
                activeDevelopments,
                ownerOfMostKnights,
                ownerOfLongestRoad,
                playerColor,
                resources = resourceList(),
                cards = cards
            )
        }
    }
}

data class PlayerDto(
    val userName: String,
    val activeCards: List<DevelopmentCard>,
    val ownerOfMostKnights: Boolean,
    val ownerOfLongestRoad: Boolean,
    val playerColor: PlayerColor,
    val resources: List<ResourceType>? = null,
    val cards: List<DevelopmentCard>? = null,
    val hiddenResources: Int? = null,
    val hiddenDevelopments: Int? = null
)

enum class PlayerColor {
    RED,
    GREEN,
    BLUE,
    YELLOW
}

