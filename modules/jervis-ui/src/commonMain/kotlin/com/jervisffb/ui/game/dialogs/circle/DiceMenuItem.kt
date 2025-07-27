package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId

/**
 * Class representing a "dice" menu item in the action wheel.
 */
class DiceMenuItem<T: DieResult>(
    override val parent: ActionWheelMenuItem,
    val id: DieId,
    value: T,
    options: List<T>,
    val preferLtr: Boolean = true,
    enabled: Boolean = true,
    val expandable: Boolean = true,
    startAnimationFrom: T? = null,
    val onHover: (T?) -> Unit = { },
): ActionWheelMenuItem() {
    override val label: () -> String = { "Dice[${value::class.simpleName}]" }
    override val expandMode: MenuExpandMode = MenuExpandMode.NONE
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    override var enabled: Boolean by mutableStateOf(enabled)
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
        if (item is DiceMenuItem<*>) {
            enabled = !expanded || !(parent == item.parent && id != item.id)
        }
    }

    private fun reorderOptions() {
        val updatedList  = originalOptions.toMutableList().also {
            it.remove(this@DiceMenuItem.value)
            it.add(0, this@DiceMenuItem.value)
        }
        diceList = updatedList
    }

    fun valueSelected(value: T) {
        this.value = value
        reorderOptions()
    }
}
