package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult

class ReportInjuryResult(val context: RiskingInjuryContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (!context.armourBroken) {
            append("${context.player.name}'s armour held up.")
        } else {
            when (context.injuryResult!!) {
                InjuryResult.STUNNED -> append("${context.player.name} was Stunned")
                InjuryResult.KO -> {
                    if (context.apothecaryUsed != null) {
                        append("${context.player.name} was Stunned")
                    } else {
                        append("${context.player.name}'s was Knocked Out")
                    }
                }
                InjuryResult.BADLY_HURT -> append("${context.player.name} was Badly Hurt")
                InjuryResult.CASUALTY -> {
                    val (casualtyResult, lastingInjuryResult) = if (context.isPartOfMultipleBlock) {
                        (context.casualtyResult!! to context.lastingInjuryResult)
                    } else {
                        context.finalCasualtyResult!! to context.finalLastingInjury
                    }
                    when (casualtyResult) {
                        CasualtyResult.BADLY_HURT -> append("${context.player.name} was Badly Hurt")
                        CasualtyResult.SERIOUS_HURT -> append("${context.player.name} was Serious Hurt")
                        CasualtyResult.SERIOUS_INJURY -> append("${context.player.name} gained a Serious Injury")
                        CasualtyResult.LASTING_INJURY -> {
                            append("${context.player.name} got a Lasting Injury: ${lastingInjuryResult!!.description}")
                        }
                        CasualtyResult.DEAD -> append("${context.player.name} was killed DEAD!")
                    }
                }
            }
        }
    }
}
