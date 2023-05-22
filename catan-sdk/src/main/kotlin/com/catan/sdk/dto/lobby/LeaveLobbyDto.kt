package com.catan.sdk.dto.lobby

class LeaveLobbyDto(
    sessionId: String,
    val lobbyId: String
) : LobbyBase(sessionId, LEAVE)
