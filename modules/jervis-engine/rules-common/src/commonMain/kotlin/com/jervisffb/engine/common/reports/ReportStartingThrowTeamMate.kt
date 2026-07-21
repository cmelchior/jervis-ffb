package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportStartingThrowTeamMate(val context: ThrowTeamMateContext, target: PitchCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} threw ${context.thrownPlayer?.name ?: "<Unknown>"} to ${target.toLogString()}")
    }
}
