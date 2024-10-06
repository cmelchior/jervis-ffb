package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.PlayerAction

class ReportActionEnded(player: Player, action: PlayerAction) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} ended ${action.name}."
}
