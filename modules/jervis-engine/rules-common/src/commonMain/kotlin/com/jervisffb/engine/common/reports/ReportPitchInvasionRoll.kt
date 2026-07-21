package com.jervisffb.engine.common.reports

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPitchInvasionRoll(team: Team, roll: D6Result, fanFactor: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Fans from ${team.name} invaded the pitch [ ${roll.value} + $fanFactor = ${roll.value + fanFactor} ]"
}
