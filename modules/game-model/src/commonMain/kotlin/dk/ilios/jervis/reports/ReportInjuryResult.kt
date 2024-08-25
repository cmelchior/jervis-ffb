package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext

class ReportInjuryResult(val context: RiskingInjuryRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            val msg = """
                Armour Roll: ${context.armourRoll} + ${context.armourModifiers}
                Injury Roll: ${context.injuryRoll} + ${context.injuryModifiers}
                Casualty Roll: ${context.casualtyRoll} + ${context.casualtyModifiers}
            """.trimIndent()
            return msg
        }
}
