package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team

class ReportStartingKickOff(kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${kickingTeam.name} is kicking off"
}
