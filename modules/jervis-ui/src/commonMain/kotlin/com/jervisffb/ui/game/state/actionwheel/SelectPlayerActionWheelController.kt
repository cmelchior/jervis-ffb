package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.ActivatePlayer
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ActionButtonData
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper

/**
 * Select a player action after selecting the player.
 */
object SelectPlayerActionWheelController : ActionWheelDialogController() {
    override val nodes: Set<Node> = setOf(ActivatePlayer.DeclareActionOrDeselectPlayer)

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper
    ) {
        val wheelOptions = actions.get<SelectPlayerAction>().actions.map {
            val id = ButtonId("[$it.id.value] ${it.type}")
            createActionOption(id, acc.game, provider, it)
        }.toMutableList()

        // If prone, also add a "Stand Up & And Action". But only if the
        // action has a move component. Similar to FUMBBL.
        // TODO If the player has Jump Up, all non-move actions can also do this.
        val activePlayer = acc.game.activePlayer ?: error("No active player")
        if (activePlayer.state == PlayerState.PRONE) {
            // val oldData = acc.fieldSquares[activePlayer.location as FieldCoordinate]!!
            val menuItem = ActionButtonData(
                id = ButtonId("[${actions.id.value}] Stand Up & End Action"),
                label = { "Stand Up & End Action" },
                action = {
                    // If players need to stand up or roll for negatraits before standing
                    // up we need wait for it.
                    provider.registerQueuedActionGenerator { controller ->
                        val availableActions = controller.getAvailableActions()
                        val canMove = availableActions.contains(MoveType.STANDARD)
                        val canEndAction = availableActions.contains<EndActionWhenReady>()
                        if (canMove && canEndAction) {
                            QueuedActionsResult(EndAction)
                        } else {
                            null
                        }
                    }
                    provider.userActionSelected(
                        CompositeGameAction(PlayerActionSelected(PlayerStandardActionType.MOVE), MoveTypeSelected(MoveType.STAND_UP))
                    )
                },
                icon = ActionIcon.STAND_UP_AND_END
            )
            wheelOptions.add(menuItem)
        }
        val onDismiss = {
            provider.userActionSelected(PlayerDeselected(activePlayer))
        }
        val wheelState = ActionWheelUiStateData(
            center = activePlayer.coordinates,
            bottomItems = wheelOptions,
            bottomExpandMode = MenuExpandMode.FanOut(spread = 360f),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = onDismiss,
            hideWhenClickOutside = true
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Temporary work-around while transition from FieldDecorator api
    private fun createActionOption(
        id: ButtonId,
        state: Game,
        provider: UiActionProvider,
        action: PlayerAction
    ): ActionButtonData {
        return state.activePlayer?.location?.let { location ->
            val (actionName, actionIcon) = when (action.type) {
                PlayerStandardActionType.MOVE -> "Move" to ActionIcon.MOVE
                PlayerStandardActionType.PASS -> "Pass" to ActionIcon.PASS
                PlayerStandardActionType.HAND_OFF -> "Hand-off" to ActionIcon.HANDOFF
                PlayerStandardActionType.BLOCK -> "Block" to ActionIcon.BLOCK
                PlayerStandardActionType.BLITZ -> "Blitz" to ActionIcon.BLITZ
                PlayerStandardActionType.FOUL -> "Foul" to ActionIcon.FOUL
                PlayerStandardActionType.SPECIAL -> "Special" to ActionIcon.CONFIRM // What to do here?
                PlayerStandardActionType.THROW_TEAM_MATE -> "Throw Team-mate" to ActionIcon.THROW_TEAM_MATE
                PlayerStandardActionType.SECURE_THE_BALL -> "Secure the Ball" to ActionIcon.SECURE_THE_BALL
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
            ActionButtonData(
                id = id,
                label = { actionName },
                icon = actionIcon,
                action = { provider.userActionSelected(PlayerActionSelected(action.type)) },
                enabled = true,
            )
        } ?: error("No active player")
    }
}
