package dk.ilios.bowlbot.logs

class ReportStartingDrive(drive: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting drive: $drive"
}
