@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.common.context.DesperateMeasuresRollContext
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.procedures.DesperateMeasuresRoll
import com.jervisffb.engine.common.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.NoOpGuardedAction
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime

abstract class SingleDiceWithLabelRollWheelController: ActionWheelDialogController() {

    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val diceRollType: DiceRollType
    override val nodes: Set<Node> by lazy {
        setOf(rollDiceNode)
    }
    abstract fun allUiOptions(state: Game): List<DieResult>
    open fun getLabel(state: Game, die: DieResult): String? = null

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val allOptions = allUiOptions(acc.game)
        if (nodes.contains(acc.stack.currentNode())) {
            val buttons = allOptions.map { dieResult ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${dieResult.value}"),
                    label = { getLabel(acc.game, dieResult) },
                    diceValue = dieResult,
                    action = GuardedAction(acc) { id -> provider.userActionSelected(id, dieResult) },
                    options = allOptions,
                    expandable = false,
                    diceRollType = diceRollType,
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = buttons,
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                bottomAnimationType = ButtonLayoutMode.HIDE,
                onDismiss = null,
                animationOnly = false
            )
            acc.addActionWheelEvent(wheelState)
        }
    }

    // Animate the result when it was not selected in the UI.
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val currentNode = acc.stack.currentNode()
        if (!((nodes.contains(currentNode)) && shouldAnimateAction(acc))) return false

        val dieValue = when (selectedAction) {
            is DiceRollResults -> selectedAction.rolls.first()
            is DieResult -> selectedAction
            else -> error("Unsupported action: $selectedAction")
        }

        val button = dieValue.let { roll ->
            val buttonId = ButtonId("$buttonIdPrefix-${roll.value}")
            DieButtonData(
                id = buttonId,
                label = { "" },
                diceRollType = diceRollType,
                diceValue = roll,
                action = NoOpGuardedAction,
                options = emptyList(),
                expandable = false,
                enabled = false,
                animateRoll = RollAnimationData(
                    endValue = roll,
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
}

object PrayersToNuffleRollWheelController : SingleDiceWithLabelRollWheelController() {
    override val buttonIdPrefix: String = "prayers"
    override val rollDiceNode: Node = PrayersToNuffleRoll.RollDie
    override val diceRollType: DiceRollType = DiceRollType.PRAYERS_TO_NUFFLE
    override fun allUiOptions(state: Game): List<DieResult> {
        return state.rules.prayersToNuffleTable.die.allOptions
    }
    override fun getLabel(state: Game, die: DieResult): String {
        return state.rules.prayersToNuffleTable.roll(die).description
    }
    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<PrayersToNuffleRollContext>()
        return getTeamCenterCoordinates(context.team)
    }
}

object DesperateMeasuresRollWheelController : SingleDiceWithLabelRollWheelController() {
    override val buttonIdPrefix: String = "desperate-measures"
    override val rollDiceNode: Node = DesperateMeasuresRoll.RollDie
    override val diceRollType: DiceRollType = DiceRollType.DESPERATE_MEASURES
    override fun allUiOptions(state: Game): List<DieResult> {
        TODO("Not yet implemented")
    }

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<DesperateMeasuresRollContext>()
        return getTeamCenterCoordinates(context.team)
    }
}
