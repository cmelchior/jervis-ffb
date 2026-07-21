package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportTimeout(state: Game, kickingTurnNo: Int, receivingTurnNo: Int, moveForward: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (moveForward) {
            appendLine("${state.kickingTeam.name} moves turn marker forward to $kickingTurnNo")
            append("${state.receivingTeam.name} moves turn marker forward to $receivingTurnNo")
        } else {
            appendLine("${state.kickingTeam.name} moves turn marker back to $kickingTurnNo")
            append("${state.receivingTeam.name} moves turn marker back to $receivingTurnNo")
        }
    }
}

