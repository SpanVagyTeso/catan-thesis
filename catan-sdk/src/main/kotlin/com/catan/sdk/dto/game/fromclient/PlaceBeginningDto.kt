package com.catan.sdk.dto.game.fromclient

import com.catan.sdk.dto.game.fromclient.FromClientPayloadType.PlaceBeginning

class PlaceBeginningDto(
    sessionId: String,
    val edgeId: String,
    val vertexId: String
) : FromClient(sessionId, PlaceBeginning)
