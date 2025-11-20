package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportForegoActivation(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} foregoes their activation"
}
