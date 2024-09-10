package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult

class ReportInjuryResult(val context: RiskingInjuryContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return if (!context.armourBroken) {
                "${context.player.name}'s armour held up."
            } else {
                when (context.injuryResult!!) {
                    InjuryResult.STUNNED -> "${context.player.name}'s was stunned."
                    InjuryResult.KO -> "${context.player.name}'s was knocked out."
                    InjuryResult.BADLY_HURT -> "${context.player.name}'s was badly hurt."
                    InjuryResult.CASUALTY -> {
                        when (context.casualtyResult!!) {
                            CasualtyResult.BADLY_HURT -> "${context.player.name} was badly hurt."
                            CasualtyResult.SERIOUS_HURT -> "${context.player.name} was Serious Hurt."
                            CasualtyResult.SERIOUS_INJURY -> "${context.player.name}'s gained a Serious Injury."
                            CasualtyResult.LASTING_INJURY -> "${context.player.name}'s got a Lasting Injury."
                            CasualtyResult.DEAD -> "${context.player.name}'s was killed DEAD!"
                        }
                    }
                }
            }
        }
}
