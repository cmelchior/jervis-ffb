package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.bb2020.tables.TableResult

class ReportKickOffEventRoll(firstDie: D6Result, secondDie: D6Result, result: TableResult) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Rolled ${result.description} on the Kick-Off Table"
}
