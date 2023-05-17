package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.STARTUP
import com.catan.sdk.entities.MapDto
import com.catan.sdk.entities.MaritimeLocations
import com.catan.sdk.entities.PlayerDto

class StartupDto(
    val startingPlayer: String,
    val map: MapDto,
    val players: List<PlayerDto>,
    val maritimeLocations: List<MaritimeLocations>
) : FromServer(STARTUP)
