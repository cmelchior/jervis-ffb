package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.common.procedures.actions.throwteammate.ThrowTeamMateContext

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
