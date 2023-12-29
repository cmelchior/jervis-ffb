package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team

class ReportSetupReceivingTeam(receivingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${receivingTeam.name} is setting up to receive."
}
