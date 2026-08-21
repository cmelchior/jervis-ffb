package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.bb2020.procedures.blitz.BlitzAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.model.GuardedPlayerAction
import com.jervisffb.ui.game.model.UiPlayerAction
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.state.calculateAssumedNoOfBlockDice

object SelectPlayerDecorator: PitchActionDecorator<SelectPlayer> {
    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: SelectPlayer,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        descriptor.players.forEach { playerId ->
            val action = PlayerSelected(playerId)
            val selectedAction = UiPlayerAction(action, GuardedPlayerAction(acc) { id, _, _ ->
                actionProvider.userActionSelected(id, action)
            })

            val playerLocation = state.getPlayerById(playerId).location

            // Calculate dice decorators (if any)
            var dice = when (state.stack.currentNode()) {
                BlockAction.SelectDefenderOrEndAction -> {
                    val attacker = state.activePlayer!!
                    val defender = state.getPlayerById(playerId)
                    calculateAssumedNoOfBlockDice(state, attacker, defender, isBlitzing = false)
                }
                com.jervisffb.engine.bb2025.procedures.actions.block.BlockAction.SelectDefenderOrEndAction -> {
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
                Dogout -> {
                    acc.updatePlayer(playerId) {
                        it.copy(
                            looksSelectable = true,
                            selectedAction = selectedAction.takeIf { isEnabled }
                        )
                    }
                }
                is PitchCoordinate -> {
                    if (acc.squares.containsKey(playerLocation)) {
                        acc.updateSquare(playerLocation) {
                            it.copy(
                                isSelectable = false,
                                selectedAction = null,
                                isActionWheelFocus = false
                            )
                        }
                    }
                    acc.updatePlayer(playerId) {
                        it.copy(
                            dice = dice,
                            looksSelectable = true,
                            selectedAction = selectedAction.takeIf { isEnabled },
                        )
                    }
                }
                is GiantLocation -> TODO("Not supported right now")
            }
        }
    }
}
