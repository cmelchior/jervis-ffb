package dk.ilios.bowlbot.logs

import kotlin.random.Random

class SimpleLogEntry(
    override val message: String,
    override val category: LogCategory = LogCategory.STATE_MACHINE,
    override val id: Long = Random.nextLong()
): LogEntry {
    override fun toString(): String {
        return "SimpleLogEntry(message='$message', category=$category, id=$id)"
    }
}
