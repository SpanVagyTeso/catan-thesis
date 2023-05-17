package com.catan.sdk.dto.lobby

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.LOBBY_BASE

const val CREATE = "CREATE"
const val CREATED = "CREATED"
const val REFRESH = "REFRESH"
const val START = "START"
const val JOIN = "JOIN"
const val LOBBY_INFO = "LOBBY_INFO"
const val LEAVE = "LEAVE"
const val AVAILABLE_LOBBIES = "AVAILABLELOBBIES"
const val GET_AVAILABLE_LOBBIES = "GETAVAILABLELOBBIES"

open class LobbyBase(
    val sessionId: String? = null,
    val commandType: String
) : DtoType(LOBBY_BASE)
