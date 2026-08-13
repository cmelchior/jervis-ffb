@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRerollDice
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRollDice
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
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
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime

/**
 * Control rolling block dice.
 * This is only used when dice rolls are done locally.
 */
object StandardBlockRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        StandardBlockRollDice.RollDice,
        StandardBlockRerollDice.ReRollDie,
        SingleStandardBlockRollDice.RollDice,
        SingleStandardBlockRerollDice.ReRollDie,
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<BlockContext>().defender.coordinates
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val currentNode = acc.gameController.currentNode()
        val request = actions.get<RollDice>()
        val diceButtons = if (currentNode in listOf(StandardBlockRerollDice.ReRollDie, SingleStandardBlockRerollDice.ReRollDie)) {
            // This is a re-roll, so we want to show all dice, even the ones we do not re-roll,
            // they are just not selectable.
            val context = acc.game.getContext<BlockContext>()
            val diceRoll = context.roll
            val rerollContext = acc.game.getRerollContext()
            val rerollDice = rerollContext.rerollDice ?: error("No reroll was selected")

            diceRoll.mapIndexed { index, die ->
                val isEnabled = (die in rerollDice)
                DieButtonData(
                    id = ButtonId("block-$index"),
                    label = { null },
                    diceRollType = DiceRollType.BLOCK,
                    diceValue = die.result,
                    action = { /* Do nothing */ },
                    enabled = isEnabled,
                    options = DBlockResult.allOptions(),
                    expandable = isEnabled,
                    preferLtr = (index == 0),
                )
            }
        } else {
            // This is not a reroll, so just show the number of dice provided by the Action Request.
            List(request.dice.size) { index ->
                DieButtonData(
                    id = ButtonId("block-$index"),
                    label = { null },
                    diceRollType = DiceRollType.BLOCK,
                    diceValue = DBlockResult.random(),
                    action = { /* Do nothing */ },
                    enabled = true,
                    options = DBlockResult.allOptions(),
                    expandable = true,
                    preferLtr = (index == 0),
                )
            }
        }

        // When re-rolling dice, we use the same dice id's as used in StandardBlockChooseResultOrRerollWheelController
        // to make the animations nicer.
        val buttonId = if (currentNode == SingleStandardBlockRerollDice.ReRollDie) {
            val context = acc.game.getRerollContext()
            val rerollDescription = context.source?.rerollDescription ?: ""
            ButtonId("reroll-$rerollDescription")
        } else {
            ButtonId("confirm")
        }

        val actionButtons = listOf(
            ActionButtonData(
                id = buttonId,
                label = { "Confirm Roll" },
                icon = ActionIcon.CONFIRM,
                action = {
                    val dice = diceButtons.mapNotNull {
                        when (it.enabled) {
                            true -> it.diceValue
                            false -> null
                        }
                    }
                    provider.userActionSelected(DiceRollResults(dice))
                },
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
        val rolledDice = selectedAction.safeCast<DiceRollResults>().rolls.map { it as DBlockResult }
        if (shouldAnimateAction(acc)) {
            val rerollContext = acc.game.getRerollContextOrNull()
            val isReroll = (rerollContext != null && rerollContext.rerollDice != null)
            val diceButtons = if (isReroll) {
                // Map the rerolled dice back to their position in the original roll.
                val dicePool = rerollContext.originalRoll
                val rerollDice = rerollContext.rerollDice ?: error("No reroll was selected")
                val rolledDieIds = rolledDice.indices.map { index -> rerollDice[index].id }
                dicePool.mapIndexed { index, die ->
                    val indexRolled = rolledDieIds.indexOfFirst { it == die.id }
                    val newValue = if (indexRolled != -1) rolledDice[indexRolled] else null
                    DieButtonData(
                        id = ButtonId("block-$index"),
                        label = { null },
                        diceRollType = DiceRollType.BLOCK,
                        diceValue = newValue ?: die.result,
                        action = { /* Do nothing */ },
                        options = DBlockResult.allOptions(),
                        expandable = false,
                        enabled = (newValue != null),
                        animateRoll = if (newValue != null) {
                            RollAnimationData(endValue = newValue)
                        } else {
                            null
                        }
                    )
                }
            } else {
                rolledDice.mapIndexed { index, die ->
                    DieButtonData(
                        id = ButtonId("block-$index"),
                        label = { null },
                        diceRollType = DiceRollType.BLOCK,
                        diceValue = die,
                        action = { /* Do nothing */ },
                        options = DBlockResult.allOptions(),
                        expandable = false,
                        animateRoll = RollAnimationData(
                            endValue = die
                        ),
                    )
                }
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
                bottomMessage = DiceRollType.BLOCK.description
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
