package com.catan.sdk.dto.game.fromserver

import com.catan.sdk.dto.game.fromserver.FromServerPayloadType.CHANGEONBOARD

data class ChangeOnBoardDto(
    val id: String,
    val changeType: ChangeType,
    val username: String? = null
) : FromServer(CHANGEONBOARD)

enum class ChangeType {
    ROAD,
    CITY,
    VILLAGE,
    REMOVEBLOCK,
    ADDBLOCK
}
