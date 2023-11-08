package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Game

class SimpleLogEntry(
    private val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
): LogEntry {
    override fun render(state: Game): String = message
}
