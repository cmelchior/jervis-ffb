package dk.ilios.jervis.reports

data class ReportStartingHalf(private val half: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting half: $half"
}
