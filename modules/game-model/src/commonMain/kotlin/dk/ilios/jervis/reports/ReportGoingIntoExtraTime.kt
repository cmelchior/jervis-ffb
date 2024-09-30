package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Game

class ReportGoingIntoExtraTime(
    private val state: Game,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("${state.homeTeam.name} goes ${state.awayScore} - ${state.homeScore} against ${state.awayTeam.name}")
        append("Game goes into Extra Time")
    }
}

