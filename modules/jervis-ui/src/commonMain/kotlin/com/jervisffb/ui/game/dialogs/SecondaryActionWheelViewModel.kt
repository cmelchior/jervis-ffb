package com.jervisffb.ui.game.dialogs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.view.ContextMenuOption
import com.jervisffb.ui.menu.LocalFieldDataWrapper

/**
 * Action-Wheel View Model for "context" actions, i.e., actions that are not
 * considered primary at the current Node. This wheel thus needs to be opened
 * by clicking a player before actions can be used. It can also be dismissed
 * without generating a [com.jervisffb.engine.actions.GameAction].
 */
class SecondaryActionWheelViewModel(
    coordinates: FieldCoordinate?,
    options: List<ContextMenuOption>,
    team: Team,
    sharedFieldData: LocalFieldDataWrapper,
    onMenuHidden: (() -> Unit)? = null,
): AbstractActionWheelViewModel(
        team,
        sharedFieldData,
    ) {

    val initialData: ActionWheelUiState
    val hideAction = com.jervisffb.ui.game.view.HideActionWheel(hideImmediately = true)
    val data: MutableState<ActionWheelUiState> = mutableStateOf(hideAction)

    init {
        hideOnClickedOutside.value = true
        initialData = ActionWheelUiStateData(
            center = coordinates,
            bottomItems = options.map { contextOption ->
                ActionButtonData(
                    id = ButtonId("context-${contextOption.title}"),
                    label = { contextOption.title },
                    icon = contextOption.icon,
                    action = {
                        hideWheel()
                        contextOption.command()
                    },
                    enabled = true
                )
            },
            bottomExpandMode = MenuExpandMode.FanOut(spread = 360f),
            bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
            onDismiss = { onMenuHidden?.invoke() },
            hideWhenClickOutside = true
        )
        data.value = initialData
    }

    override fun showWheel() {
        sharedFieldData.setContextActionWheelVisibility(true)
        data.value = initialData
    }

    override fun hideWheel(onDismiss: (() -> Unit)?) {
        sharedFieldData.let {
            it.setContextActionWheelVisibility(false)
            data.value = hideAction
            // Do not use onDismiss here as the Context Menu doesn't generate GameActions
        }
    }
}
