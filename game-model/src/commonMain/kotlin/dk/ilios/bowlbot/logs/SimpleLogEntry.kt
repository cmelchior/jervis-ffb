package dk.ilios.bowlbot.logs

import kotlin.random.Random

data class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
    override val id: Long = Random.nextLong()
): LogEntry
