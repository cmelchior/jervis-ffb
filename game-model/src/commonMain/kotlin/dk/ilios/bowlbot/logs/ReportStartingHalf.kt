package dk.ilios.bowlbot.logs

import kotlin.random.Random

data class ReportStartingHalf(private val half: Int) : LogEntry {
    override val id: Long = Random.nextLong()
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting half: $half"
}
