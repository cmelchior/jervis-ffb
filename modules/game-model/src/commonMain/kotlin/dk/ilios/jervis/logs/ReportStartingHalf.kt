package dk.ilios.jervis.logs

data class ReportStartingHalf(private val half: UInt) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting half: $half"
}
