package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Game

class ReportGetTheRef(state: Game) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("Get the Ref! The teams beat up the referee.")
        appendLine("${state.homeTeam.name} received +1 Bribe")
        append("${state.awayTeam.name} received +1 Bribe")

    }
}

