package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.calculateAssumedNoOfBlockDice

class SelectPlayerDecorator: FieldActionDecorator<SelectPlayer> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectPlayer) {
        descriptor.players.forEach { player ->
            val selectedAction = {
                actionProvider.userActionSelected(PlayerSelected(player))
            }

            val playerLocation = state.getPlayerById(player).location

            // Calculate dice decorators (if any)
            var dice = when (state.stack.currentNode()) {
                BlockAction.SelectDefenderOrEndAction -> {
                    val attacker = state.activePlayer!!
                    val defender = state.getPlayerById(player)
                    calculateAssumedNoOfBlockDice(state, attacker, defender, isBlitzing = false)
                }
                BlitzAction.MoveOrBlockOrEndAction -> {
                    val attacker = state.activePlayer!!
                    val defender = state.getPlayerById(player)
                    calculateAssumedNoOfBlockDice(state, attacker, defender, isBlitzing = true)
                }
                else -> 0
            }

            // Depending on the location, the event is tracked slightly different
            when (playerLocation) {
                DogOut -> {
                    snapshot.dogoutActions[player] = selectedAction
                }
                is FieldCoordinate -> {
                    val square = snapshot.fieldSquares[playerLocation]
                    snapshot.fieldSquares[playerLocation] = square?.copy(
                        dice = dice,
                        onSelected = selectedAction
                    ) ?: error("Unexpected player location : $playerLocation")
                }
                is GiantLocation -> TODO("Not supported right now")
            }
        }
    }
}
