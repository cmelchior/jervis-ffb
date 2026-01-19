package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.ui.game.dialogs.AbstractActionWheelViewModel
import com.jervisffb.ui.game.dialogs.ButtonId

/**
 * Class representing the "top"-level node of a menu system. This menu only
 * has submenu items and no "primary". It is an entry point owned by
 * [ActionWheelMenuController].
 */
class TopLevelMenuItem(
    val viewModel: AbstractActionWheelViewModel,
    originAngle: Float,
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf(),
    override var expandMode: MenuExpandMode = MenuExpandMode.Compact()
): ActionWheelMenuItem() {
    override val id: ButtonId = ButtonId("TopLevel")
    override var label: () -> String = { "TopLevel" }
    val size: Int get() = subMenu.size
    override val parent: ActionWheelMenuItem? = null
    override var enabled: Boolean = true

    var selectedMenuItem: ActionWheelMenuItem? by mutableStateOf(null)
    override val defaultStartAngle: Float = originAngle

    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        // Do nothing
    }

    init {
        startSubAngle = originAngle
    }
}
