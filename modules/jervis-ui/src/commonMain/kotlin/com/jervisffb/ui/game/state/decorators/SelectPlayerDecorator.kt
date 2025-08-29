package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.calculateAssumedNoOfBlockDice

object SelectPlayerDecorator: FieldActionDecorator<SelectPlayer> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectPlayer,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.players.forEach { playerId ->
            val selectedAction = {
                actionProvider.userActionSelected(PlayerSelected(playerId))
            }

            val playerLocation = state.getPlayerById(playerId).location

            // Calculate dice decorators (if any)
            var dice = when (state.stack.currentNode()) {
                BlockAction.SelectDefenderOrEndAction -> {
                    val attacker = state.activePlayer!!
                    val defender = state.getPlayerById(playerId)
                    calculateAssumedNoOfBlockDice(state, attacker, defender, isBlitzing = false)
                }
                BlitzAction.MoveOrBlockOrEndAction -> {
                    val attacker = state.activePlayer!!
                    val defender = state.getPlayerById(playerId)
                    calculateAssumedNoOfBlockDice(state, attacker, defender, isBlitzing = true)
                }
                else -> 0
            }

            // Depending on the location, the event is tracked slightly different
            when (playerLocation) {
                DogOut -> {
                    acc.updatePlayer(playerId) {
                        it.copy(
                            selectedAction = selectedAction
                        )
                    }
                }
                is FieldCoordinate -> {
                    acc.updateSquare(playerLocation) {
                        it.copy(
                            selectedAction = null,
                            isActionWheelFocus = false
                        )
                    }
                    acc.updatePlayer(playerId) {
                        it.copy(
                            dice = dice,
                            selectedAction = selectedAction,
                        )
                    }
                }
                is GiantLocation -> TODO("Not supported right now")
            }
        }
    }
}
