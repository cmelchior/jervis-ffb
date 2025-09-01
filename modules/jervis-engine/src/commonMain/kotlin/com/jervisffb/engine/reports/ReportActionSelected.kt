package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.actions.PlayerAction

class ReportActionSelected(player: Player, action: PlayerAction) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} selected action: ${action.name}."
}
