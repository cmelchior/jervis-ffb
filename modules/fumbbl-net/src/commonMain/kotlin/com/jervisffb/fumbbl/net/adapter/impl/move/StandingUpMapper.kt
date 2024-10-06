package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.PlayerStandardActionType

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
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
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
