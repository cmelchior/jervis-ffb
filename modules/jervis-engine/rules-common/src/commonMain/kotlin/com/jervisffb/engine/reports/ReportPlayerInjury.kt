package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState

class ReportPlayerInjury(player: Player, state: PlayerState) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val name = (state as? Enum<*>)?.name ?: "Unknown"
        append("${player.name} is $name")
    }
}
