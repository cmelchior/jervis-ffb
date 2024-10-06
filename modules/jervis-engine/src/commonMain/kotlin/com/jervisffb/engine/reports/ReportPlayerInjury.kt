package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState

class ReportPlayerInjury(player: Player, state: PlayerState) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} is ${state.name}."
}
