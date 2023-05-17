package com.catan.sdk.entities

import com.catan.sdk.entities.ResourceType.*

enum class FieldType {
    FOREST,
    PASTURE,
    MOUNTAINS,
    HILLS,
    FIELDS,
    DESERT,
    OCEAN
}

enum class ResourceType {
    Grain,
    Lumber,
    Wool,
    Brick,
    Ore
}

enum class TradeType {
     FourToOne,
    ThreeToOne,
    TwoToOneWood,
    TwoToOneSheep,
    TwoToOneBrick,
    TwoToOneOre,
    TwoToOneWheat
}

enum class BuildType {
    VILLAGE,
    CITY,
    ROAD
}

enum class DevelopmentTypes {
    Knight,
    YearOfPlenty,
    RoadBuilding,
    Monopoly,
    University,
    Market,
    Library,
    Chapel,
    GreatHall
}

fun FieldType.toResourceType() = when (this) {
    FieldType.FOREST -> Lumber
    FieldType.PASTURE -> Wool
    FieldType.MOUNTAINS -> Ore
    FieldType.HILLS -> Brick
    FieldType.FIELDS -> Grain
    else -> {
        null
    }
}
