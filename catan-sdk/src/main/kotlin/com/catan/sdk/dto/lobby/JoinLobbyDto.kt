package com.catan.sdk.dto.lobby

class JoinLobbyDto(
    val lobbyId: String,
    sessionId: String
) : LobbyBase(
    sessionId,
    JOIN,
)
