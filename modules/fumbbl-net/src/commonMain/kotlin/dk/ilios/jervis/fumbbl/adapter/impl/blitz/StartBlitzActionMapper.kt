package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetStandingUp
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.model.reports.SelectBlitzTargetReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.ActivatePlayer
import dk.ilios.jervis.procedures.TeamTurn
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction

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
