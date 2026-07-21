package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportDeflection(
    player: Player,
    success: Boolean,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (success) {
            false -> append("${player.name} failed to deflect the ball")
            true -> append("${player.name} successfully deflected the ball")
        }
    }
}
