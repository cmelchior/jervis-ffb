package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.ui.game.icons.ActionIcon

/**
 * Class representing an "action" menu item in the action wheel. Actions are
 * anything that is not "dice" or "coins".
 */
class ActionMenuItem(
    override val parent: ActionWheelMenuItem,
    label: () -> String,
    icon: ActionIcon,
    onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
    enabled: Boolean = true,
    override val expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
): ActionWheelMenuItem() {

    override var enabled: Boolean by mutableStateOf(enabled)
    override var label: () -> String by mutableStateOf(label)
    var icon: ActionIcon by mutableStateOf(icon)
    var onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit by mutableStateOf(onClick)

    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        // Do nothing
    }

}
