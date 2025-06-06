package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportInterception(
    player: Player,
    success: Boolean,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (success) {
            false -> append("${player.name} failed to intercept the ball")
            true -> append("${player.name} successfully intercepted the ball")
        }
    }
}
