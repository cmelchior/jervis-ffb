package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportRecoverPlayer(player: Player, recovered: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = when (recovered) {
        true -> "${player.name} recovered successfully and moved to Reserves"
        false -> "${player.name} failed to recover"
    }
}
