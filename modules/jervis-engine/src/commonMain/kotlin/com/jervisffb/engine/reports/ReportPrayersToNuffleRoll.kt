package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.tables.TableResult

class ReportPrayersToNuffleRoll(
    team: Team,
    dieRoll: D16Result,
    result: TableResult,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} rolled ${dieRoll.toLogString()} on the Prayers of Nuffle Table: ${result.description}"
}
