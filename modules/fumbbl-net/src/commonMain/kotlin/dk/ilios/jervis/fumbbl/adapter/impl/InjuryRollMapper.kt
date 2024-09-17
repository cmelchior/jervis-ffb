package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.InjuryReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.tables.injury.ArmourRoll
import dk.ilios.jervis.procedures.tables.injury.CasualtyRoll
import dk.ilios.jervis.procedures.tables.injury.InjuryRoll
import dk.ilios.jervis.procedures.tables.injury.LastingInjuryRoll

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

        if (report.armorRoll?.isNotEmpty() == true) {
            val armourRoll = report.armorRoll.map { D6Result(it) }
            newActions.add(DiceResults(armourRoll), ArmourRoll.RollDice)
        }

        if (report.injuryRoll?.isNotEmpty() == true) {
            val injuryRoll = report.injuryRoll.map { D6Result(it) }
            newActions.add(DiceResults(injuryRoll), InjuryRoll.RollDice)
        }

        if (report.casualtyRoll?.isNotEmpty() == true) {
            val casualtyRoll = report.casualtyRoll.first().let { D16Result(it) }
            newActions.add(DiceResults(casualtyRoll), CasualtyRoll.RollDie)
            if (casualtyRoll.value in 13..14) {
                val lastingCasualtyRoll = report.casualtyRoll.last().let { D6Result(it) }
                newActions.add(DiceResults(lastingCasualtyRoll), LastingInjuryRoll.RollDie)
            }
        }
    }
}
