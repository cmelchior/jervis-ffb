@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.DeviateRoll
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ActionButtonData
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.DieButtonData
import com.jervisffb.ui.game.dialogs.RollAnimationData
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Control Deviate rolls (1D8 + 1D6).
 */
object DeviateRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(DeviateRoll.RollDice)

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val diceButtons = listOf(
            // We want D6 on the "right" side, so it has to go first in the list
            // as we start on the clockwise direction
            DieButtonData(
                id = ButtonId("deviate-d6"),
                label = { "Distance" },
                diceValue = D6Result.random(),
                action = { /* Do nothing */ },
                options = D6Result.allOptions(),
                expandable = true,
            ),
            DieButtonData(
                id = ButtonId("deviate-d8"),
                label = { "Direction" },
                diceValue = D8Result.random(),
                action = { /* Do nothing */ },
                options = D8Result.allOptions(),
                expandable = true,
                preferLtr = false,
            ),
        )

        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = {
                    RandomDirectionTemplate.getTemplateValues().toMap()[diceButtons[1].diceValue]?.let { direction ->
                        "Confirm Roll: $direction${diceButtons[0].diceValue.value}"
                    } ?: "Confirm Roll"
                },
                icon = ActionIcon.CONFIRM,
                action = {
                    // The UI has the dice in a different order than the one expected by the rules
                    // engine, so reverse them to match the rules.
                    val dice = diceButtons.reversed().map {
                        it.diceValue
                    }
                    provider.userActionSelected(DiceRollResults(dice))
                }
            )
        )

        val wheelState = ActionWheelUiStateData(
            center = acc.game.currentBall().location,
            topItems = diceButtons,
            topExpandMode = MenuExpandMode.Compact(),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomItems = actionButtons,
            bottomExpandMode = MenuExpandMode.Compact(),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val dice = selectedAction.safeCast<DiceRollResults>().let { diceResults ->
            diceResults.rolls.last() as D6Result to diceResults.first() as D8Result
        }
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        if (serverRoll) {
            val diceButtons = listOf(
                DieButtonData(
                    id = ButtonId("deviate-d6"),
                    label = { null },
                    diceValue = dice.first,
                    action = { /* Do nothing */ },
                    options = D6Result.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = dice.first,
                    )
                ),
                DieButtonData(
                    id = ButtonId("deviate-d8"),
                    label = { null },
                    diceValue = dice.second,
                    action = { /* Do nothing */ },
                    options = D8Result.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = dice.second,
                        additionalDelayAfterRoll = DEFAULT_DELAY_AFTER_ROLL
                    ),
                    preferLtr = false,
                ),
            )
            val wheelState = ActionWheelUiStateData(
                center = acc.game.currentBall().location,
                topItems = diceButtons,
                topExpandMode = MenuExpandMode.Compact(),
                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
                bottomItems = emptyList(),
                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = true
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
