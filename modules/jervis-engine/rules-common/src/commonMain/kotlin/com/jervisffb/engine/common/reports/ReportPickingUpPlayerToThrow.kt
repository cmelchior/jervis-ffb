package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.common.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportPickingUpPlayerToThrow(val context: ThrowTeamMateContext, thrownPlayer: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} picked up ${thrownPlayer.name}")
    }
}
