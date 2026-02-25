@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRerollDice
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRollDice
import com.jervisffb.engine.rules.builder.DiceRollOwner
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
 * Control first block roll.
 */
object StandardBlockRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        StandardBlockRollDice.RollDice,
        StandardBlockRerollDice.ReRollDie,
        SingleStandardBlockRollDice.RollDice,
        SingleStandardBlockRerollDice.ReRollDie,
    )

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val currentNode = acc.gameController.currentNode()
        val diceValues = if (currentNode == StandardBlockRerollDice.ReRollDie) {
            val context = acc.game.getContext<BlockContext>()
            context.roll.map { it.result }
        } else {
            actions.get<RollDice>().dice.map { DBlockResult.random() }
        }
        val diceButtons = diceValues.mapIndexed { index, diceValue ->
            DieButtonData(
                id = ButtonId("block-$index"),
                label = { null },
                diceValue = diceValue,
                action = { /* Do nothing */ },
                options = DBlockResult.allOptions(),
                expandable = true,
                preferLtr = (index == 0)
            )
        }

        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = { "Confirm Roll" },
                icon = ActionIcon.CONFIRM,
                action = {
                    val dice = diceButtons.map {
                        it.diceValue
                    }
                    provider.userActionSelected(DiceRollResults(dice))
                },
            )
        )

        val context = acc.game.getContext<BlockContext>()
        val wheelState = ActionWheelUiStateData(
            center = context.defender.coordinates,
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
        val dice = selectedAction.safeCast<DiceRollResults>().rolls.map { it as DBlockResult }
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        if (serverRoll) {
            val diceButtons = dice.mapIndexed { index, die ->
                DieButtonData(
                    id = ButtonId("block-$index"),
                    label = { null },
                    diceValue = die,
                    action = { /* Do nothing */ },
                    options = DBlockResult.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = die,
                    )
                )
            }
            val context = acc.game.getContext<BlockContext>()
            val wheelState = ActionWheelUiStateData(
                center = context.defender.coordinates,
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
