package dk.ilios.jervis.logs

import dk.ilios.jervis.model.Team

class ReportSetupKickingTeam(kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${kickingTeam.name} is setting up for a kick."
}
