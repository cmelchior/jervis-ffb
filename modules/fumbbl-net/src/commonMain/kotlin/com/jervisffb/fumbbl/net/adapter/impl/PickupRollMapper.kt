package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.PickupRoll
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.PickUpRollReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object PickupRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.reports.first() as PickUpRollReport
        // TODO The report gives you the final result. We need to deconstruct it
        val diceRoll = D6Result(report.roll)
        if (report.reRolled) {
            newActions.add(DiceRollResults(diceRoll), PickupRoll.ReRollDie)
        } else {
            newActions.add(DiceRollResults(diceRoll), PickupRoll.RollDie)
            newActions.add(Continue, PickupRoll.ChooseReRollSource) // TODO How to choose reroll source here?
        }
    }
}
