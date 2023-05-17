package com.catan.sdk.dto.lobby

class AvailableLobbiesDto(
    val lobbies: Array<LobbyDto>
) : LobbyBase(
    commandType = AVAILABLE_LOBBIES
)
