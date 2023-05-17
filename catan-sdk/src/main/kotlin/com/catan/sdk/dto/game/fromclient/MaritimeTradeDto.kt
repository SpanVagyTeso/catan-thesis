package com.catan.sdk.dto.game.fromclient

import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.*
import com.catan.sdk.entities.ResourceType
import com.catan.sdk.entities.TradeType

class MaritimeTradeDto (
    sessionId: String,
    val fromResource: ResourceType,
    val toResource: ResourceType,
    val tradeType: TradeType
): FromClient(sessionId, Trade)
