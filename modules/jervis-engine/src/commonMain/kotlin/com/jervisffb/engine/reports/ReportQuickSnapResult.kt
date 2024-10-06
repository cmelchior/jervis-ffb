package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Team

class ReportQuickSnapResult(receivingTeam: Team, roll: D3Result) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("Quick Snap: ${receivingTeam.name} may move [${roll.value} + 3 = ${roll.value + 3}] players")
    }
}
