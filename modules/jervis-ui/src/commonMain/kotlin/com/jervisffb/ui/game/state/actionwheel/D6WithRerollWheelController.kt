@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
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
 * Abstract class for handling all single D6 with a potential reroll like:
 *
 * - Accuracy
 * - Catch
 * - Dodge
 * - Pickup
 * - Rush
 * - Shadowing
 */
abstract class D6WithRerollWheelController() : ActionWheelDialogController() {

    // Parameters / Methods required to customize the behavior
    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val chooseRerollSourceNode: Node
    abstract val rerollDiceNode: Node
    abstract fun getActionWheelCenter(state: Game): FieldCoordinate?
    abstract fun getOriginalRoll(state: Game): D6Result
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
        sharedData: LocalFieldDataWrapper,
    ) {
        if (acc.stack.currentNode() == rollDiceNode || acc.stack.currentNode() == rerollDiceNode) {
            val buttons = D6Result.allOptions().map { d6Option ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${d6Option.value}"),
                    label = { null }, // "Roll ${d6Option.value}",
                    diceValue = d6Option,
                    action = { provider.userActionSelected(d6Option) },
                    options = D6Result.allOptions(),
                    expandable = false,
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
                center = acc.game.activePlayer?.coordinates,
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

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        val currentNode = acc.stack.currentNode()
        if ((currentNode == rollDiceNode || currentNode == rerollDiceNode) && serverRoll) {
            val button = selectedAction.safeCast<DiceRollResults>().let { roll ->
                val d6Roll = roll.rolls.first() as D6Result
                val buttonId = when (currentNode) {
                    rollDiceNode -> ButtonId("$buttonIdPrefix-${d6Roll.value}")
                    rerollDiceNode -> {
                        val originalRole = getOriginalRoll(acc.game)
                        ButtonId("$buttonIdPrefix-${originalRole.value}")
                    }
                    else -> error("Unexpected node: $currentNode")
                }
                DieButtonData(
                    id = buttonId,
                    label = { "" },
                    diceValue = d6Roll,
                    action = { /* Do nothing */ },
                    options = emptyList(),
                    expandable = false,
                    enabled = false,
                    animateRoll = RollAnimationData(
                        endValue = d6Roll,
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
                animationOnly = true
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}

/**
 * Define the Action Wheel layout when rolling for Accuracy (passing).
 */
object AccuracyWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "accuracy"
    override val rollDiceNode: Node = AccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = AccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<PassContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PassContext>()
        return context.passingRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Catch.
 */
object CatchWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "catch"
    override val rollDiceNode: Node = CatchRoll.RollDie
    override val chooseRerollSourceNode: Node = CatchRoll.ChooseReRollSource
    override val rerollDiceNode: Node = CatchRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.currentBall().location
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<CatchRollContext>()
        return context.roll!!.originalRoll
    }
}


/**
 * Define the Action Wheel layout when rolling for Dodge.
 */
object DodgeWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "dodge"
    override val rollDiceNode: Node = DodgeRoll.RollDie
    override val chooseRerollSourceNode: Node = DodgeRoll.ChooseReRollSource
    override val rerollDiceNode: Node = DodgeRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<DodgeRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Pickup.
 */
object PickupWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pickup"
    override val rollDiceNode: Node = PickupRoll.RollDie
    override val chooseRerollSourceNode: Node = PickupRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PickupRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate? {
        return state.activePlayer?.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PickupRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Rush.
 */
object RushWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "rush"
    override val rollDiceNode: Node = RushRoll.RollDie
    override val chooseRerollSourceNode: Node = RushRoll.ChooseReRollSource
    override val rerollDiceNode: Node = RushRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<RushRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Shadowing.
 */
object ShadowingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "shadowing"
    override val rollDiceNode: Node = ShadowingRoll.RollDie
    override val chooseRerollSourceNode: Node = ShadowingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ShadowingRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ShadowingRollContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ShadowingRollContext>()
        return context.roll!!.originalRoll
    }
}
