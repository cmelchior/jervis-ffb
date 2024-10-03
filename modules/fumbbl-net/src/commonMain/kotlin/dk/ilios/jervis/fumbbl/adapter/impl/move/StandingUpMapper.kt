package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.ActivatePlayer
import dk.ilios.jervis.procedures.TeamTurn
import dk.ilios.jervis.procedures.actions.move.MoveAction
import dk.ilios.jervis.rules.PlayerStandardActionType

object StandingUpMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstReport() is PlayerActionReport &&
            (command.firstReport() as PlayerActionReport).playerAction == PlayerAction.STAND_UP
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
        val report = command.firstReport() as PlayerActionReport
        newActions.add(PlayerSelected(report.actingPlayerId.toJervisId()), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(PlayerActionSelected(PlayerStandardActionType.MOVE), ActivatePlayer.DeclareActionOrDeselectPlayer)
        newActions.add(MoveTypeSelected(MoveType.STAND_UP), MoveAction.SelectMoveType)
        newActions.add(EndAction, MoveAction.SelectMoveType)
    }
}
