package statistics

import com.catan.sdk.dto.stats.GetStatsDto
import com.catan.sdk.dto.stats.StatsDto
import database.DatabaseService

class StatisticsService(
    val databaseService: DatabaseService
) {
    fun getStats(dto: GetStatsDto): StatsDto {
        val users = databaseService.getUserByUsername(dto.username)
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
