package com.catan.sdk.dto.game

import com.catan.sdk.dto.DtoType

const val GAME = "GAME"

open class Game(
    val gameType: GameType
) : DtoType(
    GAME
)

enum class GameType {
    fromClient,
    fromServer
}
