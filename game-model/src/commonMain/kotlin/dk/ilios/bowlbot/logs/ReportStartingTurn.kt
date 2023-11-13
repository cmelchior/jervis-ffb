package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Team

class ReportStartingTurn(team: Team, turn: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting turn for ${team.name}: $turn"
}
