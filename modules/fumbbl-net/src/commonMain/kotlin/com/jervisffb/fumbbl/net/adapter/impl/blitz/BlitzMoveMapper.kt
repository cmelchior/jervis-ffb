package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.StandardMoveStep
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerCoordinate
import com.jervisffb.fumbbl.net.utils.FumbblGame

object BlitzMoveMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLITZ_MOVE
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
        val hasBlocked = fumbblGame.actingPlayer.hasBlocked
        moves.forEach {
            val coord = FieldCoordinate(it.value!!.x, it.value.y)
            newActions.add(MoveTypeSelected(MoveType.STANDARD), if (hasBlocked) BlitzAction.RemainingMovesOrEndAction else BlitzAction.MoveOrBlockOrEndAction)
            newActions.add(FieldSquareSelected(coord), StandardMoveStep.SelectTargetSquareOrEndAction)
        }
    }
}
