@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.ext.d2
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeamStep
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.CoinButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Do a coin toss
 */
object CoinTossWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(DetermineKickingTeamStep.CoinToss)

    override fun getActionWheelCenter(state: Game): PitchCoordinate? = null

    // Only relevant for local-side dice rolls
    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val coinButtons = listOf(
            CoinButtonData(
                id = ButtonId("coin-toss-head"),
                label = { "Heads" },
                value = Coin.HEAD,
                action = { provider.userActionSelected(CoinTossResult(Coin.HEAD)) },
            ),
            CoinButtonData(
                id = ButtonId("coin-toss-tail"),
                label = { "Tails" },
                value = Coin.TAIL,
                action = { provider.userActionSelected(CoinTossResult(Coin.TAIL)) },
            ),
        )
        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topItems = coinButtons,
            topExpandMode = MenuExpandMode.Compact(angleBetweenItemsDegrees = 90f),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomAnimationType = ButtonLayoutMode.HIDE,
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
        if (!serverRoll) return false

        val tossResult = (selectedAction as CoinTossResult).result
        val coinButton = CoinButtonData(
            id = ButtonId("coin-toss"),
            value = tossResult,
            label = { "" },
            action = { },
            animateRoll = RollAnimationData(
                endValue = when (tossResult) {
                    Coin.HEAD -> 1.d2
                    Coin.TAIL -> 2.d2
                },
            ),
        )

        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topItems = listOf(coinButton),
            topExpandMode = MenuExpandMode.Compact(),
            topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
            bottomItems = emptyList(),
            bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = true,
            bottomMessage = "Coin Flip"
        )
        acc.addActionWheelEvent(wheelState)
        return true
    }
}
