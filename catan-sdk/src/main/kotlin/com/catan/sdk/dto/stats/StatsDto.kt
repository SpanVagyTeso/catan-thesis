package com.catan.sdk.dto.stats

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.STATS

class StatsDto(
    val username: String,
    val gamesPlayed: Int,
    val gamesWon: Int
): DtoType(STATS)

class GetStatsDto(
    val username: String
): DtoType(STATS)

