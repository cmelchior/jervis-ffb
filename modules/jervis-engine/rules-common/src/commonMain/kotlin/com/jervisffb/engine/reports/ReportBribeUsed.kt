package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportBribeUsed(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.team.name} tries to bribe the referee"
}
