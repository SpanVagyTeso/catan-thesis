package com.catan.sdk.dto.lobby

class CreateLobbyDto(
    val name: String,
    val size: Int,
    sessionId: String
) : LobbyBase(sessionId, CREATE)
