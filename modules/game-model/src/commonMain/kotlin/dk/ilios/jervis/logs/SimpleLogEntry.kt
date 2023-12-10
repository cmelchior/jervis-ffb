package dk.ilios.jervis.logs

class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
): LogEntry()
