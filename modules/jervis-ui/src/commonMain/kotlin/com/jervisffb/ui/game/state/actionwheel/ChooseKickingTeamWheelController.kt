@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.CoinTossContext
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeamStep
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.CoinButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlin.time.ExperimentalTime


object ChooseKickingTeamWheelController : ActionWheelDialogController() {
    val yesLabel: String = "Kicking Team"
    val noLabel: String = "Receiving Team"
    override val nodes: Set<Node> = setOf(
        DetermineKickingTeamStep.ChooseKickingTeam,
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        val context = state.getContext<CoinTossContext>()
        return when (context.winner!!.isAwayTeam()) {
            true -> AwayTeamFanFactorRoll.getActionWheelCenter(state)
            false -> HomeTeamFanFactorRoll.getActionWheelCenter(state)
        }
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val buttons = listOf(
            CoinButtonData(
                id = ButtonId("coin-toss"), // Same ID as from CoinTossWheelController
                value = acc.game.getContext<CoinTossContext>().coinToss!!.result,
                enabled = false,
                label = { "" },
                action = { },
            ),
            ActionButtonData(
                id = ButtonId("accept-yes"),
                label = { yesLabel },
                icon = ActionIcon.CONFIRM,
                action = { provider.userActionSelected(Confirm) },
            ),
            ActionButtonData(
                id = ButtonId("accept-no"),
                label = { noLabel },
                icon = ActionIcon.CANCEL,
                action = { provider.userActionSelected(Cancel) },
            ),
        )
        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topExpandMode = MenuExpandMode.Compact(spread = 360f, angleBetweenItemsDegrees = 90f),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            topItems = buttons,
            bottomItems = emptyList(),
            bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false
        )
        acc.addActionWheelEvent(wheelState)
    }
}
