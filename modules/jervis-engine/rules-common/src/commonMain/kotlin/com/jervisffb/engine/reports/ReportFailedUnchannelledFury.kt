package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportFailedUnchannelledFury(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} failed their Unchannelled Fury roll and stand around raging incoherently"
}
