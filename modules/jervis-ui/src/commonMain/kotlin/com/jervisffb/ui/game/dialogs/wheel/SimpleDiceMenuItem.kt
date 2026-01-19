package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.ui.game.dialogs.ButtonId

/**
 * Class representing a simple dice with no submenu items.
 * See [DiceMenuWithSubmenuSelectorMenuItem] and [DiceMenuWithPopupSelectorMenuItem]
 * for dice with more advanced capabilities.
 */
class SimpleDiceMenuItem<T: DieResult>(
    override val id: ButtonId,
    val value: T,
    val options: List<T>,
    override val parent: ActionWheelMenuItem?,
    label: () -> String,
    enabled: Boolean,
    val onClick: (DieResult) -> Unit,
    val startAnimationFrom: T? = null,
): ActionWheelMenuItem() {
    override var label: () -> String by mutableStateOf(label)
    override var enabled: Boolean by mutableStateOf(enabled)
    override val expandMode: MenuExpandMode = MenuExpandMode.None
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    var animatingFrom: T? by mutableStateOf(startAnimationFrom)
    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        // Do nothing
    }
}
