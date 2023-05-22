package service

import com.catan.sdk.dto.stats.GetStatsDto
import com.catan.sdk.dto.stats.StatsDto

class StatisticsService(
    val databaseService: DatabaseService
) {
    fun getStats(dto: GetStatsDto): StatsDto {
        val users = databaseService.getUserByusername(dto.username)
        return if (users.isEmpty()) {
            StatsDto("", -1, 0)
        } else {
            val user = users.single()
            StatsDto(
                user.userName,
                user.gamesPlayed,
                user.gamesWon
            )
        }
    }

}
