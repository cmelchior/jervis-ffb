package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPowResult(val attacker: Player, val defender: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${attacker.name} knocks down ${defender.name} with a POW!"
        }
}
