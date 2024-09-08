package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.model.Team

class ReportQuickSnapResult(receivingTeam: Team, roll: D3Result) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("Quick Snap: ${receivingTeam.name} may move [${roll.value} + 3 = ${roll.value + 3}] players")
    }
}
