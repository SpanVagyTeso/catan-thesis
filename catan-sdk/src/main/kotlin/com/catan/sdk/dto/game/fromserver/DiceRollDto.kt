package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.DICES

data class DiceRollDto(
    val dice1: Int,
    val dice2: Int
) : FromServer(DICES)
