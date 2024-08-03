package dk.ilios.jervis.reports

open class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
): LogEntry()
