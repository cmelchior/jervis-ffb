package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportStartingThrowTeamMate(val context: ThrowTeamMateContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} threw ${context.thrownPlayer?.name ?: "<Unknown>"}")
    }
}
