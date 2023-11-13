package dk.ilios.bowlbot.logs

class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
): LogEntry()
