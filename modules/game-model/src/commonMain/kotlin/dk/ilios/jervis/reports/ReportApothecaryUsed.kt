package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.inducements.Apothecary

class ReportApothecaryUsed(team: Team, apothecary: Apothecary) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} used ${apothecary.type.description}"
}
