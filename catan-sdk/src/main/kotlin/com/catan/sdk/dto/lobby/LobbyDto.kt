package com.catan.sdk.dto.lobby

class LobbyDto(
    val ownerUser: String,
    val users: Array<String>,
    val maxSize: Int,
    val lobbyName: String,
    id: String
) : LobbyBase(
    id,
    LOBBY_INFO
)
