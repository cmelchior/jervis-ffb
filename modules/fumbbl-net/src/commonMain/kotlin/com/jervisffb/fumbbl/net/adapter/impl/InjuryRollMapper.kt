package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.CasualtyRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.LastingInjuryRoll
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.InjuryReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object InjuryRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return command.firstReport() is InjuryReport
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as InjuryReport

        if (report.injuryType == "crowdpush") {
            newActions.add(
                action = { state: Game, rules: Rules ->
                    // Any of them will push the player of the field
                    val action = PushStep.SelectPushDirection.getAvailableActions(state, rules).single() as SelectDirection
                    DirectionSelected(action.directions.random())
                },
                expectedNode = PushStep.SelectPushDirection
            )
        }

        if (report.armorRoll?.isNotEmpty() == true) {
            val armourRoll = report.armorRoll.map { D6Result(it) }
            newActions.add(DiceRollResults(armourRoll), ArmourRoll.RollDice)
        }

        if (report.injuryRoll?.isNotEmpty() == true) {
            val injuryRoll = report.injuryRoll.map { D6Result(it) }
            newActions.add(DiceRollResults(injuryRoll), InjuryRoll.RollDice)
        }

        if (report.casualtyRoll?.isNotEmpty() == true) {
            val casualtyRoll = report.casualtyRoll.first().let { D16Result(it) }
            newActions.add(DiceRollResults(casualtyRoll), CasualtyRoll.RollDie)
            if (casualtyRoll.value in 13..14) {
                val lastingCasualtyRoll = report.casualtyRoll.last().let { D6Result(it) }
                newActions.add(DiceRollResults(lastingCasualtyRoll), LastingInjuryRoll.RollDie)
            }
        }
    }
}
