package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.view.ContextMenuOption
import com.jervisffb.ui.menu.LocalFieldDataWrapper

/**
 * Action Wheel View Model for "primary" actions, i.e. a game state
 * with input that _must_ be created using the action wheel. This means
 * the wheel is automatically shown (if not already visible) and it cannot
 * be dismissed.
 */
class SecondaryActionWheelViewModel(
    coordinates: FieldCoordinate?,
    options: List<ContextMenuOption>,
    team: Team,
    sharedFieldData: LocalFieldDataWrapper,
    title: String? = null,
    startHoverText: String? = null,
    fallbackToShowStartHoverText: Boolean = false,
    topExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    bottomExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    onMenuHidden: (() -> Unit)? = null,
    availableInNodes: Set<Node>? = null,
    autoShowOnNewActionButtons: Boolean = true
): AbstractActionWheelViewModel(
        team,
        sharedFieldData,
        title,
        startHoverText,
        fallbackToShowStartHoverText,
        topExpandMode,
        bottomExpandMode,
        availableInNodes,
        autoShowOnNewActionButtons
    ) {
    val data: ActionWheelUiStateData

    init {
        data = ActionWheelUiStateData(
            center = coordinates,
            bottomItems = options.map { contextOption ->
                ActionButtonData(
                    id = ButtonId("context-${contextOption.title}"),
                    label = { contextOption.title },
                    icon = contextOption.icon,
                    action = {
                        hideWheel(userUiAction = true)
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
    }

    override fun showWheel() {
        sharedFieldData.setContextWheelVisibility(true)
        isVisible.value = true
    }

    override fun hideWheel(userUiAction: Boolean, onDismiss: (() -> Unit)?) {
        sharedFieldData.let {
            if (it.isContentMenuVisible.value) {
                it.setContextWheelVisibility(false)
                isVisible.value = false
                if (!userUiAction) {
                    onDismiss?.invoke()
                }
            }
        }
    }
}
