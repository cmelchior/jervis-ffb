package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2020.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportPickingUpPlayerToThrow(val context: ThrowTeamMateContext, thrownPlayer: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.thrower.name} picked up ${thrownPlayer.name}")
    }
}
