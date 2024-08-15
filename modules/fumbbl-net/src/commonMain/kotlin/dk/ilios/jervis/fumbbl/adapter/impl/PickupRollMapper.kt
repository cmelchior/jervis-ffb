package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.PickUpRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.PickupRoll

object PickupRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
//            command .firstChangeId() == null &&
            command.firstReport() is PickUpRollReport
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
        val report = command.reportList.reports.first() as PickUpRollReport
        // TODO The report gives you the final result. We need to deconstruct it
        val diceRoll = D6Result(report.roll)
        if (report.reRolled) {
            newActions.add(DiceResults(diceRoll), PickupRoll.ReRollDie)
        } else {
            newActions.add(DiceResults(diceRoll), PickupRoll.RollDie)
            newActions.add(Continue, PickupRoll.ChooseReRollSource) // TODO How to choose reroll source here?
        }
    }
}
