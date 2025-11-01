package com.jervisffb.engine.reports

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportStartingThrowTeamMate(val context: ThrowTeamMateContext, target: FieldCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} threw ${context.thrownPlayer?.name ?: "<Unknown>"} to ${target.toLogString()}")
    }
}
