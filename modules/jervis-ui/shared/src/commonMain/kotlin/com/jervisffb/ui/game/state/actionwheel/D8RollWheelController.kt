@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.bb2025.procedures.actions.throwteammate.ThrowPlayerStep
import com.jervisffb.engine.common.context.ThrowTeamMateContext
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime

abstract class D8RollWheelController: ActionWheelDialogController() {

    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val diceRollType: DiceRollType
    override val nodes: Set<Node> by lazy {
        setOf(rollDiceNode)
    }
    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        if (nodes.contains(acc.stack.currentNode())) {
            val buttons = D8Result.allOptions(startWith = 2).map { d8Option ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${d8Option.value}"),
                    label = { null },
                    diceValue = d8Option,
                    action = { provider.userActionSelected(d8Option) },
                    options = D8Result.allOptions(),
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

    // Animate rolling the die, but only for clients with server dice rolls enabled
    // as they would already have chosen the result in `onDecorateActions`
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        val currentNode = acc.stack.currentNode()
        if (!((nodes.contains(currentNode)) && serverRoll)) return false

        val button = selectedAction.safeCast<DiceRollResults>().let { roll ->
            val d8Roll = roll.rolls.first() as D8Result
            val buttonId = ButtonId("$buttonIdPrefix-${d8Roll.value}")
            DieButtonData(
                id = buttonId,
                label = { "" },
                diceRollType = diceRollType,
                diceValue = d8Roll,
                action = { /* Do nothing */ },
                options = emptyList(),
                expandable = false,
                enabled = false,
                animateRoll = RollAnimationData(
                    endValue = d8Roll,
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

object BounceBallRollWheelController : D8RollWheelController() {
    override val buttonIdPrefix: String = "bounce"
    override val rollDiceNode: Node = Bounce.RollDirection
    override val diceRollType: DiceRollType = DiceRollType.BOUNCE
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        return state.currentBall().coordinates
    }
}

object BouncePlayerRollWheelController : D8RollWheelController() {
    override val buttonIdPrefix: String = "bounce-player"
    override val rollDiceNode: Node = ThrowPlayerStep.BouncePlayer
    override val diceRollType: DiceRollType = DiceRollType.BOUNCE
    override fun getActionWheelCenter(state: Game): PitchCoordinate? {
        return state.getContextOrNull<ThrowTeamMateContext>()?.thrownPlayer?.coordinates
    }
}
