package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.common.procedures.ActivatePlayer
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper

/**
 * Select a player action after selecting the player.
 */
object SelectPlayerActionWheelController : ActionWheelDialogController() {
    override val nodes: Set<Node> = setOf(ActivatePlayer.DeclareActionOrDeselectPlayer)

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.activePlayerOrThrow().coordinates
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper
    ) {
        val wheelOptions = actions.get<SelectPlayerAction>().actions.map {
            val id = ButtonId("player-action-${it.type}")
            createActionOption(acc, id, provider, it)
        }.toMutableList()

        // If prone, also add a "Stand Up & And Action". But only if the
        // action has a move component. Similar to FUMBBL.
        val activePlayer = acc.game.activePlayer ?: error("No active player")
        if (activePlayer.state == PlayerPitchState.PRONE) {
            val menuItem = ActionButtonData(
                id = ButtonId("[${actions.id}] Stand Up & End Action"),
                label = { "Stand Up & End Action" },
                action = GuardedAction(acc) { id -> standUpAndEndAction(provider, id, activePlayer.id) },
                icon = ActionIcon.STAND_UP_AND_END
            )
            wheelOptions.add(menuItem)
        }
        val onDismiss = GuardedAction(acc) { id ->
            provider.userActionSelected(id, PlayerDeselected(activePlayer))
        }
        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            bottomItems = wheelOptions,
            bottomExpandMode = MenuExpandMode.FanOut(spread = 360f),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = onDismiss,
            hideWhenClickOutside = true
        )
        acc.addActionWheelEvent(wheelState)
    }

    /**
     * Declare a Move action, then stand up and end the action as soon as the
     * rules allow it.
     *
     * Nega-traits like Bone Head and Really Stupid are rolled after declaring
     * the action, but before the player can stand up. Standing up might also
     * require a roll. So only the Move action is sent straight away, while the
     * rest is queued until the player is allowed to do it. If the activation
     * ends before that, e.g., by failing one of these rolls, the queued actions
     * are dropped, so they do not leak into a later activation.
     */
    internal fun standUpAndEndAction(provider: UiActionProvider, id: GameActionId, playerId: PlayerId) {
        fun activationEnded(controller: GameEngineController) = controller.state.activePlayer?.id != playerId
        provider.registerQueuedActionGenerator { controller ->
            when {
                activationEnded(controller) -> QueuedActionsResult(emptyList())
                controller.getAvailableActions().contains(MoveType.STAND_UP) -> {
                    QueuedActionsResult(MoveTypeSelected(MoveType.STAND_UP))
                }
                else -> null
            }
        }
        provider.registerQueuedActionGenerator { controller ->
            val availableActions = controller.getAvailableActions()
            val canMove = availableActions.contains(MoveType.STANDARD)
            val canEndAction = availableActions.contains<EndActionWhenReady>()
            when {
                activationEnded(controller) -> QueuedActionsResult(emptyList())
                canMove && canEndAction -> QueuedActionsResult(EndAction)
                else -> null
            }
        }
        provider.userActionSelected(id, PlayerActionSelected(PlayerStandardActionType.MOVE))
    }

    // Temporary work-around while transition from PitchDecorator api
    private fun createActionOption(
        acc: UiSnapshotAccumulator,
        id: ButtonId,
        provider: UiActionProvider,
        action: PlayerAction
    ): ActionButtonData {
        val state = acc.game
        return state.activePlayer?.location?.let { location ->
            val (actionName, actionIcon) = when (action.type) {
                PlayerStandardActionType.MOVE -> "Move" to ActionIcon.MOVE
                PlayerStandardActionType.PASS -> "Pass" to ActionIcon.PASS
                PlayerStandardActionType.HAND_OFF -> "Hand-off" to ActionIcon.HANDOFF
                PlayerStandardActionType.BLOCK -> {
                    val currentPlayer = state.activePlayer
                    val hasJumpUp = currentPlayer?.isSkillAvailable(SkillType.JUMP_UP) ?: false
                    val isProne = currentPlayer?.state == PlayerPitchState.PRONE
                    val title = when (isProne && hasJumpUp) {
                        true -> "Jump Up & Block"
                        false -> "Block"
                    }
                    title to ActionIcon.BLOCK
                }
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
                PlayerSpecialActionType.PUNT -> "Punt" to ActionIcon.PUNT
                PlayerSpecialActionType.CHOMP -> "Chomp" to ActionIcon.CHOMP
            }
            ActionButtonData(
                id = id,
                label = { actionName },
                icon = actionIcon,
                action = GuardedAction(acc) { id -> provider.userActionSelected(id, PlayerActionSelected(action.type)) },
                enabled = true,
            )
        } ?: error("No active player")
    }
}
