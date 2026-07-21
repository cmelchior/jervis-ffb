package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPlayerLandingOnAnotherPlayer(context: ThrowTeamMateContext, playerInSquare: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrownPlayer?.name} landed on ${playerInSquare.name}")
    }
}
