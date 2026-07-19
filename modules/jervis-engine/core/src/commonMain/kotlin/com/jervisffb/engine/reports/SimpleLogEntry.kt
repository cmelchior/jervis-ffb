package com.jervisffb.engine.reports

open class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory
) : LogEntry()
