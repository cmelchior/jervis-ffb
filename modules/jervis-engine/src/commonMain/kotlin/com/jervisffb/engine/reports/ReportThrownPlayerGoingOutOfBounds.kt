package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportThrownPlayerGoingOutOfBounds(val context: ThrowTeamMateContext, val scatter: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrownPlayer?.name ?: "<Unknown>"} ")
        when (scatter) {
            true -> append("scatters ")
            false -> append("bounces ")
        }
        append("out of bounds")
    }
}
