package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

class ReportStartingGame(state: Game, rules: Rules) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("Starting ${state.homeTeam.name} vs. ${state.awayTeam.name}")
        append("Using ${rules.name}")
    }
}
