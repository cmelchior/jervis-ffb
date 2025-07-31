package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Class representing the "top"-level node of a menu system. It is entry point
 * owned by [ActionWheelMenuController].
 */
class TopLevelMenuItem(
    val viewModel: ActionWheelViewModel,
    originAngle: Float,
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf(),
    override val expandMode: MenuExpandMode = MenuExpandMode.COMPACT
): ActionWheelMenuItem() {
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
