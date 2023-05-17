package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.CURRENTPLAYER

class CurrentPlayerDto(
    val userName: String,
    val isStarted: Boolean = true
) : FromServer(CURRENTPLAYER)
