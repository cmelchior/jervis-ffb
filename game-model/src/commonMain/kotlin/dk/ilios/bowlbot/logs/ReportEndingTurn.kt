package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Team

class ReportEndingTurn(team: Team, turn: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Ending turn $turn for ${team.name}"
}
