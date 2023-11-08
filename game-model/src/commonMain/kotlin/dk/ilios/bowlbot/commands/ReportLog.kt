package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.logs.LogEntry
import dk.ilios.bowlbot.model.Game

class ReportLog(private val logEntry: LogEntry) : Command {
    override fun execute(state: Game, controller: GameController) {
        controller.addLog(logEntry)
    }
    override fun undo(state: Game, controller: GameController) {
        controller.removeLog(logEntry)
    }
}
