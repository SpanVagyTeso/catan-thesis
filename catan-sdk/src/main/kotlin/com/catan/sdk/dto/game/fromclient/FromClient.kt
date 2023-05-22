package com.catan.sdk.dto.game.fromclient

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.GameType.fromClient

open class FromClient(
    val sessionId: String,
    val payloadType: FromClientPayloadType
) : Game(fromClient)


enum class FromClientPayloadType {
    Monopoly,
    YearOfPlenty,
    Steal,
    Buy,
    TwoRoad,
    Pass,
    PlaceBeginning,
    Trade,
    PlayerTrade,
    AcceptTrade
}
