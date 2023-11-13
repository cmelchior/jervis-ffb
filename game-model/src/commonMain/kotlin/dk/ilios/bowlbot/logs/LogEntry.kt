package dk.ilios.bowlbot.logs

interface LogEntry {
    val id: Long
    val category: LogCategory
    val message: String
}
