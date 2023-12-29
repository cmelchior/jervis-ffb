package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.KickOffEvent

class ReportKickOffEventRoll(firstDie: D6Result, secondDie: D6Result, event: KickOffEvent) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Rolled [${firstDie.result}, ${secondDie.result}] on the Kick-Off Table: ${event.name}"
}
