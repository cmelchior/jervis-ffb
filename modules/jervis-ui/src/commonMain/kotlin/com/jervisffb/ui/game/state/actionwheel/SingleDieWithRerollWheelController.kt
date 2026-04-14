package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.PickupRoll
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

abstract class SingleDieWithRerollWheelController<T: DieResult> : ActionWheelDialogController() {

    abstract val allOptions: List<T>

    // Parameters / Methods required to customize the behavior
    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val chooseRerollSourceNode: Node
    abstract val rerollDiceNode: Node
    abstract val diceRollType: DiceRollType
    abstract fun getOriginalRoll(state: Game): T
    override val nodes: Set<Node> by lazy {
        setOf(
            rollDiceNode,
            chooseRerollSourceNode,
            rerollDiceNode,
        )
    }
    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        if (acc.stack.currentNode() == rollDiceNode || acc.stack.currentNode() == rerollDiceNode) {
            val buttons = allOptions.map { dieOption ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${dieOption.value}"),
                    label = { null }, // "Roll ${d6Option.value}",
                    diceValue = dieOption,
                    action = { provider.userActionSelected(dieOption) },
                    options = allOptions,
                    expandable = false,
                    diceRollType = diceRollType,
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game), //
                topItems = buttons,
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                bottomAnimationType = if (acc.stack.currentNode() == PickupRoll.ReRollDie) ButtonLayoutMode.CONTRACT_NEW_SUBMENU else ButtonLayoutMode.ANIMATING_ROLL,
                onDismiss = null,
                animationOnly = false
            )
            acc.addActionWheelEvent(wheelState)
        }

        if (acc.stack.currentNode() == chooseRerollSourceNode) {
            val roll = getOriginalRoll(acc.game)
            val rolledValue = DieButtonData(
                id = ButtonId("$buttonIdPrefix-${roll.value}"),
                label = { "Accept roll: ${roll.value}" },
                diceRollType = diceRollType,
                diceValue = roll,
                action = { provider.userActionSelected(NoRerollSelected()) },
                options = emptyList(),
                expandable = false,
            )
            val rerollOptions = actions.filterIsInstance<SelectRerollOption>().firstOrNull()?.let { rerollOption ->
                rerollOption.options.map { option ->
                    val rerollSource = option.getRerollSource(acc.game)
                    ActionButtonData(
                        id = ButtonId("Reroll-${rerollSource.rerollDescription}"),
                        label = { rerollSource.rerollDescription },
                        icon = ActionIcon.TEAM_REROLL,
                        enabled = true,
                        action = { provider.userActionSelected(RerollOptionSelected(option)) }
                    )
                }
            } ?: emptyList()
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = listOf(rolledValue),
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                bottomItems = rerollOptions,
                bottomExpandMode = MenuExpandMode.Compact(),
                bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = false,
            )
            acc.addActionWheelEvent(wheelState)
        }
    }

    // Animate rolling the die, but only for clients with server dice rolls enabled
    // as they would already have chosen the result in `onDecorateActions`
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        val currentNode = acc.stack.currentNode()
        if ((currentNode == rollDiceNode || currentNode == rerollDiceNode) && serverRoll) {
            val button = selectedAction.safeCast<DiceRollResults>().let { roll ->
                @Suppress("UNCHECKED_CAST")
                val dieRoll = roll.rolls.first() as T
                val buttonId = when (currentNode) {
                    rollDiceNode -> ButtonId("$buttonIdPrefix-${dieRoll.value}")
                    rerollDiceNode -> {
                        val originalRole = getOriginalRoll(acc.game)
                        ButtonId("$buttonIdPrefix-${originalRole.value}")
                    }
                    else -> error("Unexpected node: $currentNode")
                }
                DieButtonData(
                    id = buttonId,
                    label = { "" },
                    diceRollType = diceRollType,
                    diceValue = dieRoll,
                    action = { /* Do nothing */ },
                    options = emptyList(),
                    expandable = false,
                    enabled = false,
                    animateRoll = RollAnimationData(
                        endValue = dieRoll,
                    ),
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = listOf(button),
                topExpandMode = MenuExpandMode.Compact(),
                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
                bottomItems = emptyList(),
                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = true,
                bottomMessage = diceRollType.description
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
