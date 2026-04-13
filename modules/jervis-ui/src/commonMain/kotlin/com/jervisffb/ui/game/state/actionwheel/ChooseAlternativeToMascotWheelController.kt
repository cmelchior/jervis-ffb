@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.procedures.rerolls.MascotContext
import com.jervisffb.engine.rules.common.procedures.rerolls.TeamMascotStep
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


object ChooseAlternativeToMascotWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        TeamMascotStep.ChooseAnotherReroll
    )

    override fun getActionWheelCenter(state: Game): FieldCoordinate? {
        val context = state.getContext<MascotContext>()
        val team = context.team
        return when (team.isHomeTeam()) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val topButtons = listOf(
            ActionButtonData(
                id = ButtonId("accept-yes"),
                label = { "Keep Mascot Roll" },
                icon = ActionIcon.CONFIRM,
                action = { provider.userActionSelected(NoRerollSelected()) },
            ),
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
            topItems = topButtons,
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
