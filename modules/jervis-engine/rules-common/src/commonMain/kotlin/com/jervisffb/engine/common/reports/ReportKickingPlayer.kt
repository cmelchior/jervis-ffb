package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

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
