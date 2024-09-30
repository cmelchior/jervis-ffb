package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Game

class ReportClosingGame(private val state: Game) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Game closed"
}

