package com.catan.sdk.dto.game.fromclient

import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.PlayerTrade
import com.catan.sdk.dto.game.fromserver.PlayerTradeOfferDto
import com.catan.sdk.entities.ResourceType

class PlayerTradeDto (
    sessionId: String,
    val withWho: String,
    val resource: ResourceType,
    val amount: Int,
    val toResource: ResourceType,
    val toAmount: Int
): FromClient(sessionId, PlayerTrade) {
    fun toOffer(tradeId: Int, userName: String) = PlayerTradeOfferDto(
        tradeId,
        userName,
        toResource,
        toAmount,
        resource,
        amount
    )
}

class AcceptTrade(
    sessionId: String,
    val id: Int
): FromClient(sessionId, FromClientPayloadType.AcceptTrade)


