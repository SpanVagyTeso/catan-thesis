package com.catan.sdk.dto.lobby

class StartLobbyDto(
    sessionId: String,
    val lobbyId: String
) : LobbyBase(
    sessionId,
    START
)
