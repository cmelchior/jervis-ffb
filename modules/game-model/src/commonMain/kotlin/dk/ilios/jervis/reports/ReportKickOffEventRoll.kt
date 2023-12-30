package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.TableResult

class ReportKickOffEventRoll(firstDie: D6Result, secondDie: D6Result, result: TableResult) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Rolled [${firstDie.result}, ${secondDie.result}] on the Kick-Off Table: ${result.name}"
}
