package com.jervisffb.fumbbl.net.adapter.impl.foul

import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object StartFoulActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is PlayerActionReport &&
                firstReport.playerAction == PlayerAction.FOUL_MOVE
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
        val report = command.reportList.first() as PlayerActionReport
        newActions.add(PlayerSelected(report.actingPlayerId.toJervisId()), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            { state, rules -> PlayerActionSelected(rules.teamActions.foul.type) },
            ActivatePlayer.DeclareActionOrDeselectPlayer
        )

        // There is a bug in FUMBBL, so you do not have to select the Foul target
        // when starting the action. I.e. you decide later who to foul.
        // The only way to get around this is by going forward in the logs in



//
//        val startActionCommand = processedCommands[processedCommands.size - 2]
//        if (startActionCommand.firstReport() !is PlayerActionReport) {
//            throw IllegalStateException("Unexpected state: ${startActionCommand.firstReport()}")
//        }
//        val playerStandingUp = startActionCommand.modelChangeList
//            .filterIsInstance<ActingPlayerSetStandingUp>()
//            .count { it.value } > 0
//        if (playerStandingUp) {
//            newActions.add(MoveTypeSelected(MoveType.STAND_UP), BlitzAction.MoveOrBlockOrEndAction)
//        }
    }
}
