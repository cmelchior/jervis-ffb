package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.animation.core.animate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.ui.game.dialogs.ButtonId

/**
 * Class representing a single "dice" menu item in the action wheel.
 * This item is different from [DiceMenuWithPopupSelectorMenuItem] in the sense
 * that instead of opening a pop-up dialog, selecting values is done
 * through a sub-menu (that will animate out).
 */
class DiceMenuWithSubmenuSelectorMenuItem<T: DieResult>(
    override val id: ButtonId,
    override val parent: ActionWheelMenuItem,
    selectedDice: T,
    options: List<T>,
    enabled: Boolean = true,
    expandable: Boolean = true,
    // Generics are acting up here. For now, just hack it and return later.
    onClick: (DieResult) -> Unit = { },
    startAnimationFrom: T? = null,
    val onHover: (T?) -> String? = { null },
    onRollAnimationFinish: () -> Unit = {}
): ActionWheelMenuItem() {
    override val contractMode: ButtonLayoutMode = ButtonLayoutMode.CONTRACT_NEW_SUBMENU
    override var label: () -> String by mutableStateOf({ "Dice[${value::class.simpleName}]" })
    override val expandMode: MenuExpandMode = MenuExpandMode.FanOut(spread = 360f, ignoreParent = true)
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    override var enabled: Boolean by mutableStateOf(enabled)
    var onClick: (DieResult) -> Unit by mutableStateOf(onClick)
    var expandable by mutableStateOf(expandable)
    var animatingFrom: T? by mutableStateOf(startAnimationFrom)
    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
    var value: T by mutableStateOf(selectedDice)
    var diceList: List<T> by mutableStateOf(emptyList())
    override val defaultStartAngle: Float = -90f
    override val startMainAngle: Float = -90f
    init {
        options.forEach { option ->
            this.addSimpleDiceButton(
                id = ButtonId("D6-${option.value}"),
                value = option,
                options = D6Result.allOptions(),
                parent = this,
                label = { option.value.toString() },
                onClick = {
                    @Suppress("UNCHECKED_CAST")
                    valueSelected(it as T)
                    onClick(it)
                },
                enabled = true,
                animate = false
            )
        }
    }

    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        if (item is DiceMenuWithSubmenuSelectorMenuItem<*>) {
            // enabled = !expanded || !(parent == item.parent && id != item.id)
        }
    }

    fun valueSelected(value: T) {
        this.value = value
    }
}
