package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ContextMenuOption

object SelectPlayerActionDecorator: FieldActionDecorator<SelectPlayerAction> {
    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        snapshot: UiGameSnapshot,
        descriptor: SelectPlayerAction,
        owner: Team?
    ) {
        // TODO Fix this, so we do not update each square multiple times
        descriptor.actions.forEach {
            addActionToContextMenu(actionProvider, state, snapshot, it)
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
                            QueuedActionsResult(EndAction)
                        } else {
                            null
                        }
                    }
                    actionProvider.userActionSelected(
                        CompositeGameAction(PlayerActionSelected(PlayerStandardActionType.MOVE), MoveTypeSelected(MoveType.STAND_UP))
                    )
                },
                icon = ActionIcon.STAND_UP_AND_END
            )
            snapshot.fieldSquares[activePlayer.location as FieldCoordinate] = oldData.copyAddContextMenu(menuItem)
        }
    }

    private fun addActionToContextMenu(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, action: PlayerAction) {
        state.activePlayer?.location?.let { location ->
            val (actionName, actionIcon) = when (action.type) {
                PlayerStandardActionType.MOVE -> "Move" to ActionIcon.MOVE
                PlayerStandardActionType.PASS -> "Pass" to ActionIcon.PASS
                PlayerStandardActionType.HAND_OFF -> "Hand-off" to ActionIcon.HANDOFF
                PlayerStandardActionType.BLOCK -> "Block" to ActionIcon.BLOCK
                PlayerStandardActionType.BLITZ -> "Blitz" to ActionIcon.BLITZ
                PlayerStandardActionType.FOUL -> "Foul" to ActionIcon.FOUL
                PlayerStandardActionType.SPECIAL -> "Special" to ActionIcon.CONFIRM // What to do here?
                PlayerStandardActionType.THROW_TEAM_MATE -> "Throw Team-mate" to ActionIcon.THROW_TEAM_MATE
                PlayerSpecialActionType.BALL_AND_CHAIN -> "Ball & Chain" to ActionIcon.BALL_AND_CHAIN
                PlayerSpecialActionType.BOMBARDIER -> "Bombardier" to ActionIcon.BOMBARDIER
                PlayerSpecialActionType.BREATHE_FIRE -> "Breathe Fire" to ActionIcon.BREATHE_FIRE
                PlayerSpecialActionType.CHAINSAW -> "Chainsaw" to ActionIcon.CHAINSAW
                PlayerSpecialActionType.HYPNOTIC_GAZE -> "Hypnotic Gaze" to ActionIcon.HYPNOTIC_GAZE
                PlayerSpecialActionType.KICK_TEAM_MATE -> "Kick Team-mate" to ActionIcon.KICK_TEAM_MATE
                PlayerSpecialActionType.MULTIPLE_BLOCK -> "Multiple Block" to ActionIcon.MULTIPLE_BLOCK
                PlayerSpecialActionType.PROJECTILE_VOMIT -> "Projectile Vomit" to ActionIcon.PROJECTILE_VOMIT
                PlayerSpecialActionType.STAB -> "Stab" to ActionIcon.STAB
            }

            val oldData = snapshot.fieldSquares[location]!!
            snapshot.fieldSquares[location as FieldCoordinate] =
                oldData.copyAddContextMenu(
                    ContextMenuOption(
                        title = actionName,
                        command = { actionProvider.userActionSelected(PlayerActionSelected(action.type)) },
                        icon = actionIcon
                    )
                )
        } ?: error("No active player")
    }
}
