package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.DEVELOPMENTCARDSREMAINING

class DevelopmentCardsRemainingDto(
    val remaining: Int
) : FromServer(DEVELOPMENTCARDSREMAINING)
