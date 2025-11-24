package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportKickingPlayer(player: Player?) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (player != null) {
            append("${player.number}. ${player.name} is kicking the ball")
        } else {
            append("No player is available to kick the ball")
        }
    }
}
