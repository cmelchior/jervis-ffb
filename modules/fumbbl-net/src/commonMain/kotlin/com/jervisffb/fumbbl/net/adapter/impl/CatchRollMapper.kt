package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.CatchRoll
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.TurnMode
import com.jervisffb.fumbbl.net.model.reports.CatchRollReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

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
