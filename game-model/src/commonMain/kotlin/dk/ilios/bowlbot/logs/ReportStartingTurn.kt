package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Team
import kotlin.random.Random

class ReportStartingTurn(team: Team, turn: Int) : LogEntry {
    override val id: Long = Random.nextLong()
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting turn for ${team.name}: $turn"

    override fun toString(): String {
        return "ReportStartingTurn(id=$id, category=$category, message='$message')"
    }
}
