package com.catan.sdk.dto.game

import com.catan.sdk.dto.game.fromclient.FromClient
import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.*
import com.catan.sdk.entities.ResourceType


class MonopolyDto(
    sessionId: String,
    val resourceType: ResourceType
) : FromClient(sessionId, Monopoly)

class YearOfPlentyDto(
    sessionId: String,
    val resource1: ResourceType,
    val resource2: ResourceType
) : FromClient(sessionId, YearOfPlenty)

class StealDto(
    sessionId: String,
    val tileId: String,
    val isKnight: Boolean,
    val fromWho: String?,
) : FromClient(sessionId, Steal)

class TwoRoadDto(
    sessionId: String,
    val edgeId1: String,
    val edgeId2: String?,
) : FromClient(sessionId, TwoRoad)


