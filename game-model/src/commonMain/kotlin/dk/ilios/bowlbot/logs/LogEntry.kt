package dk.ilios.bowlbot.logs

import kotlin.random.Random

interface LogEntry {
    val id: Long
    val category: LogCategory
    val message: String
}
