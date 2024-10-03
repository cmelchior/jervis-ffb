package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.TurnMode
import dk.ilios.jervis.fumbbl.model.reports.CatchRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.CatchRoll

object CatchRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.reports.first() as CatchRollReport
        // TODO The report gives you the final result. We need to deconstruct it
        val diceRoll = D6Result(report.roll)
        if (report.reRolled) {
            newActions.add(DiceRollResults(diceRoll), CatchRoll.ReRollDie)
        } else {
            newActions.add(DiceRollResults(diceRoll), CatchRoll.RollDie)
            // What if a player has the Catch skill?
            if (fumbblGame.turnMode != TurnMode.KICKOFF) {
                newActions.add(Continue, CatchRoll.ChooseReRollSource)
            }
        }
    }
}
