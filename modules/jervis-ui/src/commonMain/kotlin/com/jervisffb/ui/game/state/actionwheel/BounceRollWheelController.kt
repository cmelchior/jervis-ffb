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
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.DieButtonData
import com.jervisffb.ui.game.dialogs.HideActionWheel.center
import com.jervisffb.ui.game.dialogs.RollAnimationData
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Control Bounce rolls.
 */
object BounceRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(Bounce.RollDirection)

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val buttons = RandomDirectionTemplate.getTemplateValues().map { (d8Option, label) ->
            DieButtonData(
                id = ButtonId("bounce-${d8Option.value}"),
                label = { label },
                diceValue = d8Option,
                action = { provider.userActionSelected(d8Option) },
                options = D6Result.allOptions(),
                expandable = false,
            )
        }
        val wheelState = ActionWheelUiStateData(
            center = acc.game.currentBall().location,
            topItems = buttons,
            topExpandMode = MenuExpandMode.FanOut(spread = 360f),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
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
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        if (serverRoll) {
            val button = selectedAction.safeCast<DiceRollResults>().let { diceRolls ->
                val d8 = diceRolls.rolls.first() as D8Result
                val buttonId = ButtonId("bounce-${d8.value}")
                DieButtonData(
                    id = buttonId,
                    label = { "" },
                    diceValue = d8,
                    action = { /* Do nothing */ },
                    options = emptyList(),
                    expandable = false,
                    enabled = false,
                    animateRoll = RollAnimationData(
                        endValue = d8,
                    ),
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = acc.game.currentBall().location,
                topItems = listOf(button),
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
