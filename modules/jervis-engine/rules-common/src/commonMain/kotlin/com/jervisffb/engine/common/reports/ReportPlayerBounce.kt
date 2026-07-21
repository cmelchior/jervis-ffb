package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.common.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportPlayerBounce(context: ThrowTeamMateContext, target: PitchCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} bounces to ${target.toLogString()}")
    }
}
