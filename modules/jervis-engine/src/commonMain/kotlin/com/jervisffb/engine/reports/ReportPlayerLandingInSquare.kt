package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowTeamMateContext

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
