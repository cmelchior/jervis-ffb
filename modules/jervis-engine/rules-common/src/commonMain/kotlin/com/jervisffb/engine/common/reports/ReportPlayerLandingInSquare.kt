package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPlayerLandingInSquare(context: ThrowTeamMateContext, rollContext: LandingRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (rollContext.isSuccess) {
            append("${context.thrownPlayer?.name} landed successfully in ${context.target!!.toLogString()}")
        } else {
            append("${context.thrownPlayer?.name} failed to land in ${context.target!!.toLogString()}")
        }
    }
}
