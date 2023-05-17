package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.WINNERS

class WinnersDto (
    val winners: List<String>
): FromServer(WINNERS)
