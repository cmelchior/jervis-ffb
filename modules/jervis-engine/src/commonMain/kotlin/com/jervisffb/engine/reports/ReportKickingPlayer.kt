package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportKickingPlayer(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.number}. ${player.name} is kicking the ball"
}
