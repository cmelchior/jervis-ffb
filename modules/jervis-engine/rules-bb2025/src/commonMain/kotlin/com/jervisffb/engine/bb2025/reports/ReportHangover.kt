package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportHangover(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} is suffering from a Hangover and misses the drive"
}
