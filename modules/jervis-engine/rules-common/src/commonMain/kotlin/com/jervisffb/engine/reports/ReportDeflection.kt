package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

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
