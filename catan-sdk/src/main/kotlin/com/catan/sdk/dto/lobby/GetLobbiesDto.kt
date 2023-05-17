package com.catan.sdk.dto.lobby

class GetLobbiesDto(
    sessionId: String
) : LobbyBase(
    sessionId,
    GET_AVAILABLE_LOBBIES
)
