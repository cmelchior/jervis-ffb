package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeamStep
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.CoinButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper

object SelectCoinSideWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        DetermineKickingTeamStep.SelectCoinSide
    )

    override fun getActionWheelCenter(state: Game): FieldCoordinate? {
        return when (state.receivingTeam == state.awayTeam) {
            true -> AwayTeamFanFactorRoll.getActionWheelCenter(state)
            false -> HomeTeamFanFactorRoll.getActionWheelCenter(state)
        }
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val buttons = listOf(
            CoinButtonData(
                id = ButtonId("coin-head"),
                label = { "Heads" },
                value = Coin.HEAD,
                action = { provider.userActionSelected(CoinSideSelected(Coin.HEAD)) },
            ),
            CoinButtonData(
                id = ButtonId("coin-tail"),
                label = { "Tails" },
                value = Coin.TAIL,
                action = { provider.userActionSelected(CoinSideSelected(Coin.TAIL)) },
            ),
        )
        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topAnimationType = ButtonLayoutMode.HIDE,
            bottomItems = buttons,
            bottomExpandMode = MenuExpandMode.TwoWay(direction = MenuExpandMode.TwoDirection.HORIZONTAL),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false
        )
        acc.addActionWheelEvent(wheelState)
    }
}
