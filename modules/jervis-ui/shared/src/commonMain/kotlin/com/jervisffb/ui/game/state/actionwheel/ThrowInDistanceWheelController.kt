@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.common.context.ThrowInContext
import com.jervisffb.engine.common.procedures.ThrowIn
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.NoOpGuardedAction
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Control Throw-in Distance rolls (2D6) or (1D6).
 */
object ThrowInDistanceWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        ThrowIn.RollDistance,
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<ThrowInContext>().outOfBoundsAt
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val isBB7 = (acc.gameController.rules.gameType == GameType.BB7)

        val firstD6 = DieButtonData(
            id = ButtonId("throwin-distance-d6-1"),
            label = { "Distance" },
            diceRollType = DiceRollType.THROWIN_DISTANCE,
            diceValue = D6Result.random(),
            action = NoOpGuardedAction,
            options = D6Result.allOptions(),
            expandable = true,
        )
        val secondD6 = when (!isBB7) {
            true -> {
                DieButtonData(
                    id = ButtonId("throwin-distance-d6-2"),
                    label = { "Distance" },
                    diceRollType = DiceRollType.THROWIN_DISTANCE,
                    diceValue = D6Result.random(),
                    action = NoOpGuardedAction,
                    options = D6Result.allOptions(),
                    expandable = true,
                )
            }
            false -> null
        }

        val diceButtons = when (isBB7) {
            true -> listOf(firstD6)
            false -> listOf(firstD6, secondD6!!)
        }

        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = { "Confirm Roll" },
                icon = ActionIcon.CONFIRM,
                action = GuardedAction(acc) { id ->
                    // Re-order the UX button data so it matches the order expected by the rules engine
                    val dice = when (isBB7) {
                        true -> listOf(diceButtons.first().diceValue)
                        false -> listOf(diceButtons[1].diceValue, diceButtons[0].diceValue)
                    }
                    provider.userActionSelected(id, DiceRollResults(dice))
                }
            )
        )

        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topItems = diceButtons,
            topExpandMode = MenuExpandMode.Compact(),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomItems = actionButtons,
            bottomExpandMode = MenuExpandMode.Compact(),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false,
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Animate the result when it was not selected in the UI.
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val dice = selectedAction.safeCast<DiceRollResults>().let { diceResults ->
            Pair(
                diceResults[0] as D6Result,
                diceResults.getOrElse(1) { null } as? D6Result
            )
        }
        if (shouldAnimateAction(acc)) {
            val firstD6 = DieButtonData(
                id = ButtonId("throwin-distance-d6-1"),
                label = { null },
                diceRollType = DiceRollType.THROWIN_DISTANCE,
                diceValue = dice.first,
                action = NoOpGuardedAction,
                options = D6Result.allOptions(),
                expandable = false,
                animateRoll = RollAnimationData(
                    endValue = dice.first,
                ),
            )
            val secondD6 = when (dice.second != null) {
                true -> {
                    DieButtonData(
                        id = ButtonId("throwin-distance-d6-2"),
                        label = { null },
                        diceRollType = DiceRollType.THROWIN_DISTANCE,
                        diceValue = dice.first,
                        action = NoOpGuardedAction,
                        options = D6Result.allOptions(),
                        expandable = false,
                        animateRoll = RollAnimationData(
                            endValue = dice.second!!,
                        ),
                    )
                }
                false -> null
            }

            // The dice order should match the one in `onDecorateActions`
            val isBB7 = (dice.second == null)
            val diceButtons = when (isBB7) {
                true -> listOf(firstD6)
                false -> listOf(firstD6, secondD6!!)
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = diceButtons,
                topExpandMode = MenuExpandMode.Compact(),
                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
                bottomItems = emptyList(),
                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = true,
                bottomMessage = "Throw-in Distance Roll"
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
