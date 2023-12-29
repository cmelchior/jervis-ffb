package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team

class ReportEndingTurn(team: Team, turn: UInt) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Ending turn $turn for ${team.name}"
}
