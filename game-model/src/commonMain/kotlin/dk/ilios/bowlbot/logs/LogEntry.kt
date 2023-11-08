package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Game

interface LogEntry {
    val category: LogCategory
    fun render(state: Game): String
}
