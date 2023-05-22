package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.TRADEOFFER
import com.catan.sdk.entities.ResourceType

class PlayerTradeOffersDto(
    val offers: List<PlayerTradeOfferDto>
): FromServer(TRADEOFFER)

class PlayerTradeOfferDto (
    val tradeId: Int,
    val withWho: String,
    val resource: ResourceType,
    val amount: Int,
    val toResource: ResourceType,
    val toAmount: Int,
)
