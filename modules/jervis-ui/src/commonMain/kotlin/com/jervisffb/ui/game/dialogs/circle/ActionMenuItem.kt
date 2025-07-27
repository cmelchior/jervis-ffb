package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.ui.game.icons.ActionIcon

/**
 * Class representing an "action" menu item in the action wheel. Actions are
 * anything that is not "dice" or "coins".
 */
class ActionMenuItem(
    override val parent: ActionWheelMenuItem,
    override val label: () -> String,
    val icon: ActionIcon,
    val onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
    override val enabled: Boolean = true,
    override val expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
): ActionWheelMenuItem() {

    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        // Do nothing
    }

}
