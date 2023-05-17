package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.Game
import com.catan.sdk.dto.game.GameType.fromServer

open class FromServer(
    val payloadType: FromServerPayloadType
) : Game(fromServer)

enum class FromServerPayloadType {
    STARTUP,
    CHANGEONBOARD,
    DICES,
    RESOURCES,
    SEVEN,
    CURRENTPLAYER,
    DEVELOPMENTCARDSREMAINING,
    WINNERS,
    SOMEONELEFT
}
