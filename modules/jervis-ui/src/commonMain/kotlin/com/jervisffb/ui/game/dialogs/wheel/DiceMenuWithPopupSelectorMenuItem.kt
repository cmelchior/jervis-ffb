package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.ui.game.dialogs.ButtonId

/**
 * Class representing a single "dice" menu item in the action wheel.
 * This menu item can be expanded with a pop-up dialog that shows all
 * dice options.
 *
 * This is different from [DiceMenuWithSubmenuSelectorMenuItem] that will
 * show the dice selector through a submenu.
 */
class DiceMenuWithPopupSelectorMenuItem<T: DieResult>(
    override val id: ButtonId,
    override val parent: ActionWheelMenuItem,
    val diceId: DieId,
    value: T,
    options: List<T>,
    val preferLtr: Boolean = true,
    enabled: Boolean = true,
    expandable: Boolean = true,
    // Generics are acting up here. For now, just hack it and return later.
    onClick: (DieResult) -> Unit = { },
    startAnimationFrom: T? = null,
    val onHover: (T?) -> String? = { null },
): ActionWheelMenuItem() {
    override var label: () -> String by mutableStateOf({ "Dice[${value::class.simpleName}]" })
    override val expandMode: MenuExpandMode = MenuExpandMode.None
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    override var enabled: Boolean by mutableStateOf(enabled)
    var onClick: (DieResult) -> Unit by mutableStateOf(onClick)
    var expandable by mutableStateOf(expandable)
    var animatingFrom: T? by mutableStateOf(startAnimationFrom)
    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
    var value: T by mutableStateOf(value)
    var diceList: List<T> by mutableStateOf(emptyList())

    // We assume this is sorted
    private val originalOptions = options

    init {
        reorderOptions()
    }

    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        if (item is DiceMenuWithPopupSelectorMenuItem<*>) {
            enabled = !expanded || !(parent == item.parent && id != item.id)
        }
    }

    private fun reorderOptions() {
        val updatedList  = originalOptions.toMutableList().also {
            it.remove(this@DiceMenuWithPopupSelectorMenuItem.value)
            it.add(0, this@DiceMenuWithPopupSelectorMenuItem.value)
        }
        diceList = updatedList
    }

    fun valueSelected(value: T) {
        this.value = value
        reorderOptions()
    }
}
