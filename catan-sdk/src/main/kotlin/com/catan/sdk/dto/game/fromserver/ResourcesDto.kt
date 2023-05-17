package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.RESOURCES
import com.catan.sdk.entities.PlayerDto

data class ResourcesDto(
    val players: List<PlayerDto>
) : FromServer(RESOURCES)
