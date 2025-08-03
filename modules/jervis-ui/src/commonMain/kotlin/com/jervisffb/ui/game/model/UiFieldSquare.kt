package com.jervisffb.ui.game.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.MenuExpandMode
import com.jervisffb.ui.game.view.ContextMenuOption

/**
 * Represents all information needed to render a single square
 */
class UiFieldSquare(
    // "Static state", i.e. state that is not related to any given action
    val model: FieldSquare,
) {
    var isBallOnGround: Boolean by mutableStateOf(false)
    var isBallExiting: Boolean by mutableStateOf(false)
    var isBallCarried: Boolean by mutableStateOf(false)
    var player: UiPlayer? by mutableStateOf(null)
    var moveUsed: Int? by mutableStateOf(null) // Indicate how many move steps the user used to reach this square
    // Indicate the amount of move used to reach a potential target square.
    // This number will override moveused
    var futureMoveValue: Int? by mutableStateOf(null)
    // Action triggered when square is entered as part of an UI hover action
    var hoverAction: (() -> Unit)? by mutableStateOf(null)

    // State that are related to actions
    var selectableDirection: Direction? by mutableStateOf(null) // Show selectable direction arrow (i.e. with hover effect)
    var directionSelected: Direction? by mutableStateOf(null) // Show a direction arrow in its selected state
    var dice: Int by mutableStateOf(0) // Show block dice decorator
    var requiresRoll: Boolean by mutableStateOf(false) // onSelected is not-null but will result in dice being rolled
    var onSelected: (() -> Unit)? by mutableStateOf(null) // Action if square is selected
    var onMenuHidden: (() -> Unit)? by mutableStateOf(null) // Action if the context menu is hidden
    var showContextMenu = mutableStateOf(false) // The context menu is automatically opened
    var contextMenuOptions: SnapshotStateList<ContextMenuOption> = SnapshotStateList() // The options inside the context menu
    val useActionWheel = true // Whether to use a Context Menu or Action Wheel for the context items

    fun isEmpty() = !isBallOnGround && player == null

    fun copyAddContextMenu(item: ContextMenuOption, showContextMenu: Boolean? = null): UiFieldSquare {
        if (showContextMenu != null) {
            this.showContextMenu.value = showContextMenu
            this.contextMenuOptions += item
        } else {
            this.contextMenuOptions += item
        }
        return this
    }

    fun createActionWheelContextMenu(): ActionWheelInputDialog {
        val team = player?.model?.team ?: error("Player must be set for a context menu to be shown")
        val viewModel = ActionWheelViewModel(
            team = team,
            center = model.coordinates,
            startHoverText = null,
            fallbackToShowStartHoverText = false,
            bottomExpandMode = MenuExpandMode.FAN_OUT,
            shown = showContextMenu,
            hideOnClickedOutside = true,
            onMenuHidden = onMenuHidden
        ).also { wheelModel ->
            contextMenuOptions.forEach { option ->
                wheelModel.bottomMenu.addActionButton(
                    label = { option.title },
                    icon = option.icon,
                    enabled = true,
                    onClick = { parent, button ->
                        option.command()
                        wheelModel.hideWheel()
                    }
                )
            }
        }
        return ActionWheelInputDialog(
            owner = team,
            viewModel = viewModel,
        )
    }


}


