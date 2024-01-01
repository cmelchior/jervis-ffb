package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.tables.TableResult

class ReportPrayersOfNuffleRoll(
    team: Team,
    dieRoll: D16Result,
    result: TableResult
): LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} rolled ${dieRoll.toLogString()} on the Prayers of Nuffle Table: ${result.name}"
}
