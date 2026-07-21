package com.jervisffb.engine.common.reports

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportQuickSnapResult(receivingTeam: Team, roll: D3Result, extraPlayers: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("Quick Snap: ${receivingTeam.name} may move [${roll.value} + $extraPlayers = ${roll.value + extraPlayers}] players")
    }
}
