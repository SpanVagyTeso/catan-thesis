package com.catan.sdk.dto.game.fromclient

import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.Buy

enum class BuyType {
    ROAD,
    VILLAGE,
    CITY,
    UPGRADE
}

class BuyDto(
    sessionId: String,
    val buyType: BuyType,
    val id: String? = null
) : FromClient(sessionId, Buy)
