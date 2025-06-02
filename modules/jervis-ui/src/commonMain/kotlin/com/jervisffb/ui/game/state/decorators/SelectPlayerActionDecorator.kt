package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.QueuedActions
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ContextMenuOption

class SelectPlayerActionDecorator: FieldActionDecorator<SelectPlayerAction> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectPlayerAction) {
        // TODO Fix this, so we do not update each square multiple times
        descriptor.actions.forEach {
            handleAction(actionProvider, state, snapshot, it)
        }

        // If prone, also add a "Stand Up & And Action". But only if the
        // action has a move component. Similar to FUMBBL.
        // TODO If the player has Jump Up, all non-move actions can also do this.
        val activePlayer = state.activePlayer ?: error("No active player")
        if (activePlayer.state == PlayerState.PRONE) {
            val oldData = snapshot.fieldSquares[activePlayer.location as FieldCoordinate]!!
            val menuItem = ContextMenuOption(
                title = "Stand Up & End Action",
                command = {
                    // If players need to stand up or roll for negatraits before standing
                    // up we need wait for it.
                    actionProvider.registerQueuedActionGenerator { controller ->
                        val availableActions = controller.getAvailableActions()
                        val canMove = availableActions.contains(MoveType.STANDARD)
                        val canEndAction = availableActions.contains(EndAction)
                        if (canMove && canEndAction) {
                            QueuedActions(EndAction)
                        } else {
                            null
                        }
                    }
                    actionProvider.userActionSelected(
                        snapshot.nextActionId,
                        CompositeGameAction(PlayerActionSelected(PlayerStandardActionType.MOVE), MoveTypeSelected(MoveType.STAND_UP))
                    )
                }
            )
            snapshot.fieldSquares[activePlayer.location as FieldCoordinate] = oldData.copyAddContextMenu(menuItem)
        }
    }

    private fun handleAction(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, action: PlayerAction) {
        state.activePlayer?.location?.let { location ->
            val oldData = snapshot.fieldSquares[location]!!
            snapshot.fieldSquares[location as FieldCoordinate] =
                oldData.copyAddContextMenu(
                    action.let {
                        val name = when (it.type) {
                            PlayerStandardActionType.MOVE -> "Move"
                            PlayerStandardActionType.PASS -> "Pass"
                            PlayerStandardActionType.HAND_OFF -> "Hand-off"
                            PlayerStandardActionType.BLOCK -> "Block"
                            PlayerStandardActionType.BLITZ -> "Blitz"
                            PlayerStandardActionType.FOUL -> "Foul"
                            PlayerStandardActionType.SPECIAL -> "Special"
                            PlayerStandardActionType.THROW_TEAM_MATE -> "Throw Team-mate"
                            PlayerSpecialActionType.BALL_AND_CHAIN -> "Ball & Chain"
                            PlayerSpecialActionType.BOMBARDIER -> "Bombardier"
                            PlayerSpecialActionType.BREATHE_FIRE -> "Breathe Fire"
                            PlayerSpecialActionType.CHAINSAW -> "Chainsaw"
                            PlayerSpecialActionType.HYPNOTIC_GAZE -> "Hypnotic Gaze"
                            PlayerSpecialActionType.KICK_TEAM_MATE -> "Kick Team-mate"
                            PlayerSpecialActionType.MULTIPLE_BLOCK -> "Multiple Block"
                            PlayerSpecialActionType.PROJECTILE_VOMIT -> "Projectile Vomit"
                            PlayerSpecialActionType.STAB -> "Stab"
                        }
                        ContextMenuOption(
                            title = name,
                            command = { actionProvider.userActionSelected(snapshot.nextActionId, PlayerActionSelected(it.type)) },
                        )
                    },
                )
        } ?: error("No active player")
    }
}
