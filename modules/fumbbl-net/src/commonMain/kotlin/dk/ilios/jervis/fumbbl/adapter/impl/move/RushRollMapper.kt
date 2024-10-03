package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.GoForItRollReport
import dk.ilios.jervis.fumbbl.model.reports.ReRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.move.RushRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.RegularTeamReroll
import dk.ilios.jervis.rules.skills.SureFeet

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as GoForItRollReport
        newActions.add(D6Result(report.roll), RushRoll.RollDie)

        if (command.reportList.size == 1) {
            newActions.add(NoRerollSelected(), RushRoll.ChooseReRollSource)
        } else {
            val rerollReport = command.reportList.reports[1] as ReRollReport
            val rerolResult = command.reportList.reports[2] as GoForItRollReport
            val source = rerollReport.reRollSource

            newActions.add(
                action = { state: Game, rules: Rules ->
                    RushRoll.ChooseReRollSource.getAvailableActions(state, rules).first {
                        if (it is SelectRerollOption) {
                            when (source) {
                                "Team ReRoll" -> (it.option.source is RegularTeamReroll)
                                "Sure Feet" -> (it.option.source is SureFeet)
                                else -> false
                            }
                        } else {
                            false
                        }
                    }.let {
                        val option = it as SelectRerollOption
                        RerollOptionSelected(DiceRerollOption(option.option.source, option.option.dice))
                    }
                },
                expectedNode = RushRoll.ChooseReRollSource
            )
            newActions.add(D6Result(rerolResult.roll), RushRoll.ReRollDie)
        }
    }
}
