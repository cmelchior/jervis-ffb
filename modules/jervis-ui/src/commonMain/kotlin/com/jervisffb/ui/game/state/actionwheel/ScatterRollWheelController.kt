@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.ScatterRollContext
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Control Deviate rolls (1D8 + 1D6).
 */
object ScatterRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(ScatterRoll.RollDice)

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ScatterRollContext>().from
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val diceButtons = (1..3).map {
            DieButtonData(
                id = ButtonId("scatter-3-$it"),
                label = { "Scatter" },
                diceValue = D8Result.random(),
                action = { /* Do nothing */ },
                options = D8Result.allOptions(),
                expandable = true,
                preferLtr = (it > 1),
            )
        }
        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = {
                    // The visual elements are different than the underlying list, so map them
                    // back to the expected list
                    val stringValues = RandomDirectionTemplate.getTemplateValues().toMap()
                    val visibleButtons = listOf(diceButtons[1], diceButtons[0], diceButtons[2])
                    "Confirm Roll: " + visibleButtons.joinToString("") {
                        stringValues[it.diceValue] ?: "?"
                    }
                },
                icon = ActionIcon.CONFIRM,
                action = {
                    val dice = diceButtons.map { it.diceValue }
                    provider.userActionSelected(DiceRollResults(dice))
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
            animationOnly = false
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val dice = selectedAction.safeCast<DiceRollResults>()
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        if (serverRoll) {
            val diceButtons = (1..3).map {
                DieButtonData(
                    id = ButtonId("scatter-3-$it"),
                    label = { "Scatter" },
                    diceValue = dice.rolls[it - 1],
                    action = { /* Do nothing */ },
                    options = D8Result.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = dice.rolls[it - 1],
                    )
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
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
