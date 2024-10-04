package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.actions.foul.FoulContext

class ReportFoulResult(val foul: FoulContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            val lines = mutableListOf<String>()
            if (foul.spottedByTheRef) {
                lines.add("${foul.fouler.name} fouled ${foul.victim!!.name}, but was spotted by the ref.")
            } else {
                lines.add("${foul.fouler.name} fouled ${foul.victim!!.name}")
            }
            return lines.joinToString("\n")
        }
}
