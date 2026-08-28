package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.bb2020.procedures.rerolls.StandardTeamReroll2020
import com.jervisffb.engine.bb2020.skills.SureFeet
import com.jervisffb.engine.common.procedures.dicerolls.D6WithPlayerRerollProcedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.GoForItRollReport
import com.jervisffb.fumbbl.net.model.reports.ReRollReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object RushRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstReport() is GoForItRollReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as GoForItRollReport
        val rushRoll = fumbblRushRoll(jervisGame.rules)
        newActions.add(D6Result(report.roll), rushRoll.RollDie)

        if (command.reportList.size == 1) {
            newActions.add(NoRerollSelected(), rushRoll.ChooseReRollSource)
        } else {
            val rerollReport = command.reportList.reports[1] as ReRollReport
            val rerolResult = command.reportList.reports[2] as GoForItRollReport
            val fumbblSource = rerollReport.reRollSource
            newActions.add(
                action = { state: Game, rules: Rules ->
                    val rerollOptions = fumbblRushRoll(rules).ChooseReRollSource.getAvailableActions(state, rules)
                        .first { it is SelectRerollOption }
                        .let { it as SelectRerollOption }
                    val selectedOption = when (fumbblSource) {
                        "Team ReRoll" -> rerollOptions.options.first { it.getRerollSource(state) is StandardTeamReroll2020 }
                        "Sure Feet" -> rerollOptions.options.first { it.getRerollSource(state) is SureFeet }
                        else -> INVALID_GAME_STATE("No matching reroll: $rerollOptions")
                    }
                    RerollOptionSelected(selectedOption)
                },
                expectedNode = rushRoll.ChooseReRollSource
            )
            newActions.add(D6Result(rerolResult.roll), rushRoll.ReRollDie)
        }
    }

    private fun fumbblRushRoll(rules: Rules): D6WithPlayerRerollProcedure =
        rules.rushRoll as D6WithPlayerRerollProcedure
}
