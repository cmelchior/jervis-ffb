package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetStandingUp
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.model.reports.SelectBlitzTargetReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

/**
 * FUMBBL does things in a slightly different order for blitzes than defined in the rulebook.
 * I.e. the rulebook is:
 *  1. Declare Blitz
 *  2. Select Target
 *  3. Stand Up/Move
 *
 * FUMBBL does
 *  1. Declare Blitz
 *  2. Stand Up
 *  3. Declare Target
 *
 *  This mapper tries to account for that.
 */
object StartBlitzActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is SelectBlitzTargetReport
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
        val report = command.firstReport() as SelectBlitzTargetReport

        val movingPlayer = jervisGame.getPlayerById(PlayerId(report.attackerId.id))
        newActions.add(PlayerSelected(movingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            { state, rules -> PlayerActionSelected(rules.teamActions.blitz.type) },
            ActivatePlayer.DeclareActionOrDeselectPlayer
        )

        // Select target of the Blitz
        newActions.add(PlayerSelected(report.defenderId.toJervisId()), BlitzAction.SelectTargetOrCancel)

        // Check if the player is standing up when starting the blitz action part of the Blitz
        // TODO Unclear how this works with Move 2 or less.
        val startActionCommand = processedCommands[processedCommands.size - 2]
        if (startActionCommand.firstReport() !is PlayerActionReport) {
            throw IllegalStateException("Unexpected state: ${startActionCommand.firstReport()}")
        }
        val playerStandingUp = startActionCommand.modelChangeList
            .filterIsInstance<ActingPlayerSetStandingUp>()
            .count { it.value } > 0
        if (playerStandingUp) {
            newActions.add(MoveTypeSelected(MoveType.STAND_UP), BlitzAction.MoveOrBlockOrEndAction)
        }
    }
}
