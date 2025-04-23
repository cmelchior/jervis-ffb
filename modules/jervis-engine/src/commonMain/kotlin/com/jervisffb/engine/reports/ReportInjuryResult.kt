package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.engine.rules.bb2020.tables.InjuryResult

// This class is a mess. We need to find a better way to organize it, but it should probably wait
// until we have a better idea how to organize logs in the UI since it could affect grouping
// and indentation.
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
                InjuryResult.BADLY_HURT -> {
                    if (context.apothecaryInjuryRollSuccess) {
                        append("${context.player.name} was saved by the apothecary")

                    } else {
                        append("${context.player.name} was Badly Hurt")
                    }
                }
                InjuryResult.SERIOUSLY_HURT -> {
                    if (context.apothecaryInjuryRollSuccess) {
                        append("${context.player.name} was saved by the apothecary")

                    } else {
                        append("${context.player.name} was Seriously Hurt")
                    }
                }
                InjuryResult.DEAD -> {
                    if (context.apothecaryInjuryRollSuccess) {
                        append("${context.player.name} was saved by the apothecary")
                    } else {
                        append("${context.player.name} was killed DEAD!")
                    }
                }
                InjuryResult.CASUALTY -> {
                    val (casualtyResult, lastingInjuryResult) = if (context.isPartOfMultipleBlock) {
                        (context.casualtyResult!! to context.lastingInjuryResult)
                    } else {
                        context.finalCasualtyResult!! to context.finalLastingInjury
                    }
                    when (casualtyResult) {
                        CasualtyResult.BADLY_HURT -> append("${context.player.name} was Badly Hurt")
                        CasualtyResult.SERIOUSLY_HURT -> append("${context.player.name} was Seriously Hurt")
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
