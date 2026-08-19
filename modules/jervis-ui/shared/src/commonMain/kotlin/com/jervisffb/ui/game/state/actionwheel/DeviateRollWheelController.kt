@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.bb2025.procedures.BB7KickOffDeviateRoll
import com.jervisffb.engine.common.procedures.DeviateRoll
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
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
 * Control Deviate rolls (1D8 + 1D6) or (1D8 + 2D6).
 */
object DeviateRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        DeviateRoll.RollDice,
        BB7KickOffDeviateRoll.RollDice,
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.currentBall().coordinates
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val isBB7KickOff = (acc.stack.currentNode() == BB7KickOffDeviateRoll.RollDice)

        val d8 = DieButtonData(
            id = ButtonId("deviate-d8"),
            label = { "Direction" },
            diceRollType = DiceRollType.DEVIATE,
            diceValue = D8Result.random(),
            action = NoOpGuardedAction,
            options = D8Result.allOptions(),
            expandable = true,
            preferLtr = false,
        )
        val firstD6 = DieButtonData(
            id = ButtonId("deviate-d6-1"),
            label = { "Distance" },
            diceRollType = DiceRollType.DEVIATE,
            diceValue = D6Result.random(),
            action = NoOpGuardedAction,
            options = D6Result.allOptions(),
            expandable = true,
        )
        val secondD6 = when (isBB7KickOff) {
            true -> {
                DieButtonData(
                    id = ButtonId("deviate-d6-2"),
                    label = { "Distance" },
                    diceRollType = DiceRollType.DEVIATE,
                    diceValue = D6Result.random(),
                    action = NoOpGuardedAction,
                    options = D6Result.allOptions(),
                    expandable = true,
                )
            }
            false -> null
        }

        // The order of the buttons is a bit annoying as it changes depending o
        // us having 2 or 3. The reason is Action Wheel starts at 12 o'clock and
        // then alternates putting buttons on the wheel, starting
        // counter-clockwise, then clockwise. So once the buttons look right
        // visually, their underlying representation needs to be changed so the
        // D8 is first in the list.
        val diceButtons = when (isBB7KickOff) {
            true -> listOf(firstD6, d8, secondD6!!)
            false -> listOf(firstD6, d8)
        }
        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = {
                    RandomDirectionTemplate.getTemplateValues().toMap()[diceButtons[1].diceValue]?.let { direction ->
                        "Confirm Roll: $direction${diceButtons[0].diceValue.value}"
                    } ?: "Confirm Roll"
                },
                icon = ActionIcon.CONFIRM,
                action = GuardedAction(acc) { id ->
                    // Re-order the UX button data so it matches the order expected by the rules engine
                    val dice = when (isBB7KickOff) {
                        true -> listOf(diceButtons[1].diceValue, diceButtons[0].diceValue, diceButtons[2].diceValue)
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
            Triple(
                diceResults.first() as D8Result,
                diceResults[1] as D6Result,
                diceResults.getOrElse(2) { null } as? D6Result
            )
        }
        if (shouldAnimateAction(acc)) {
            val d8 = DieButtonData(
                id = ButtonId("deviate-d8"),
                label = { null },
                diceRollType = DiceRollType.DEVIATE,
                diceValue = dice.second,
                action = NoOpGuardedAction,
                options = D8Result.allOptions(),
                expandable = false,
                animateRoll = RollAnimationData(
                    endValue = dice.first,
                    additionalDelayAfterRoll = DEFAULT_DELAY_AFTER_ROLL
                ),
                preferLtr = false,
            )
            val firstD6 = DieButtonData(
                id = ButtonId("deviate-d6-1"),
                label = { null },
                diceRollType = DiceRollType.DEVIATE,
                diceValue = dice.first,
                action = NoOpGuardedAction,
                options = D6Result.allOptions(),
                expandable = false,
                animateRoll = RollAnimationData(
                    endValue = dice.second,
                ),
            )
            val secondD6 = when (dice.third != null) {
                true -> {
                    DieButtonData(
                        id = ButtonId("deviate-d6-2"),
                        label = { null },
                        diceRollType = DiceRollType.DEVIATE,
                        diceValue = dice.first,
                        action = NoOpGuardedAction,
                        options = D6Result.allOptions(),
                        expandable = false,
                        animateRoll = RollAnimationData(
                            endValue = dice.third!!,
                        ),
                    )
                }
                false -> null
            }

            // The dice order should match the one in `onDecorateActions`
            val isBB7KickOff = (dice.third != null)
            val diceButtons = when (isBB7KickOff) {
                true -> listOf(firstD6, d8, secondD6!!)
                false -> listOf(firstD6, d8)
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
                bottomMessage = "Deviate Roll"
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
