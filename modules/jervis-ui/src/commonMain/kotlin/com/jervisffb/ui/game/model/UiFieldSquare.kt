package com.jervisffb.ui.game.model

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.MenuExpandMode
import com.jervisffb.ui.game.view.ContextMenuOption
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents all information needed to render a single square on the field.
 * Note, players are represented by [UiFieldPlayer].
 */
data class UiFieldSquare(
    val coordinates: FieldCoordinate,
    val player: PlayerId? = null, // Reference to the player present in this field.
    val isBallOnGround: Boolean = false,
    val isBallExiting: Boolean = false,
    val isBallCarried: Boolean = false,
    val moveUsed: Int? = null, // Indicate how many move steps the user used to reach this square
    // State that are related to actions
    val selectableDirection: Direction? = null, // Show selectable direction arrow (i.e. with hover effect)
    val directionSelected: Direction? = null, // Show a direction arrow in its selected state
    // If negative, it means the defender has more strength. If positive, it means the attacker has more strength.
    val dice: Int = 0, // Show block dice decorator
    val isBlocked: Boolean = false, // Show "blocked" indicator
    val requiresRoll: Boolean = false, // onSelected is not-null but will result in dice being rolled
    val selectedAction: (() -> Unit)? = null, // Action if square is selected
    val onMenuHidden: (() -> Unit)? = null, // Action if the context menu is hidden
    val isActionWheelFocus: Boolean = false,
    val canHideContextMenu: Boolean = false,
    var showContextMenu: Boolean = false, // The context menu is automatically opened
    var contextMenuOptions: PersistentList<ContextMenuOption> = persistentListOf() // The options inside the context menu
) {
    val useActionWheel = true // Whether to use a Context Menu or Action Wheel for the context items
    fun isEmpty() = !isBallOnGround && player == null
    fun hasDirectionArrow() = directionSelected != null || selectableDirection != null

    fun createActionWheelContextMenu(state: Game): ActionWheelInputDialog {
        val team = state.getPlayerById(player ?: error("Player must be set for a context menu to be shown")).team
        val viewModel = ActionWheelViewModel(
            team = team,
            center = coordinates,
            startHoverText = null,
            fallbackToShowStartHoverText = false,
            bottomExpandMode = MenuExpandMode.FAN_OUT,
            visible = showContextMenu,
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
                        wheelModel.hideWheel(true)
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
