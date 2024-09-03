package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.tables.TableResult

class ReportKickOffEventRoll(firstDie: D6Result, secondDie: D6Result, result: TableResult) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Rolled ${result.description} on the Kick-Off Table"
}
