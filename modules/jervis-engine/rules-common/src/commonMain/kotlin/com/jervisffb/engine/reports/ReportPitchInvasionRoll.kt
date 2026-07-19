package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Team

class ReportPitchInvasionRoll(team: Team, roll: D6Result, fanFactor: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Fans from ${team.name} invaded the pitch [ ${roll.value} + $fanFactor = ${roll.value + fanFactor} ]"
}
