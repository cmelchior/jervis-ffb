package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.StandardMoveStep
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerCoordinate
import com.jervisffb.fumbbl.net.utils.FumbblGame

object MovePlayerMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.MOVE
                && command.sound == "step"
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
        val moves: List<FieldModelSetPlayerCoordinate> = command.modelChangeList.filterIsInstance<FieldModelSetPlayerCoordinate>()
        moves.forEach {
            val coord = FieldCoordinate(it.value!!.x, it.value.y)


            // FUMBBL treats Standing Up as an automatic action happening as part of normal moves,
            // this also means that when a player selects "Move", they automatically stand up, and if
            // deselected, put prone again. This is not how Jervis works, so here we try to detect the
            // first move step and insert a STAND_UP before it.
            // The cases where a player just stands up and ends action is done in StandingUpMapper.
            if (fumbblGame.actingPlayer.standingUp && fumbblGame.actingPlayer.currentMove == 3) {
                newActions.add(MoveTypeSelected(MoveType.STAND_UP), MoveAction.SelectMoveType)
            }
            newActions.add(MoveTypeSelected(MoveType.STANDARD), MoveAction.SelectMoveType)
            newActions.add(FieldSquareSelected(coord), StandardMoveStep.SelectTargetSquareOrEndAction)
        }
    }
}
