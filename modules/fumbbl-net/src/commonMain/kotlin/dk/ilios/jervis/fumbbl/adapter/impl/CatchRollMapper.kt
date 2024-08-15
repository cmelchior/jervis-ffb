package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.CatchRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.CatchRoll

object CatchRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command .firstChangeId() == null &&
            command.reportList.reports.firstOrNull() is CatchRollReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.reports.first() as CatchRollReport
        // TODO The report gives you the final result. We need to deconstruct it
        val diceRoll = D6Result(report.roll)
        if (report.reRolled) {
            newActions.add(DiceResults(diceRoll), CatchRoll.ReRollDie)
        } else {
            newActions.add(DiceResults(diceRoll), CatchRoll.RollDie)
            newActions.add(Continue, CatchRoll.ChooseReRollSource)
        }
    }
}
