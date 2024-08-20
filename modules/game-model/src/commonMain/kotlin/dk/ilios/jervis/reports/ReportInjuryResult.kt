package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext

class ReportInjuryResult(val context: RiskingInjuryRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "Armour/Injury/Casulty Result"
        }
}
