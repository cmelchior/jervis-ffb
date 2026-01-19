package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.DieId
import com.jervisffb.ui.game.dialogs.AbstractActionWheelViewModel
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.icons.ActionIcon

/**
 * Main entry point into controlling either the "top" or "bottom" menus of the
 * action wheel.
 */
class ActionWheelMenuController(
    // main view model for controlling the action wheel
    viewModel: AbstractActionWheelViewModel,
    // Where is considered the "starting point" from this menu
    startingAngle: Float,
    // How does this menu item expand its sub items?
    expandMode: MenuExpandMode
) {
    val topLevelMenu = TopLevelMenuItem(viewModel, startingAngle, expandMode = expandMode)
    val menuItems: MutableList<ActionWheelMenuItem> = topLevelMenu.subMenu
//    var initialStack: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf(topLevelMenu)
    var layoutMode = ButtonLayoutMode.EXPEND_NEW_SUBMENU

    // Poor mans notifier for Compose that can use this as a key to trigger updates for this class
    var version = mutableStateOf(0)

    fun notifyChange() {
        version.value += 1
    }

    /**
     * Returns the values of all dice as a [DiceRollResults] game action.
     */
    fun getDiceResults(): DiceRollResults {
        return menuItems
            .filterIsInstance<DiceMenuWithPopupSelectorMenuItem<*>>()
            .map { it.value }
            .let { DiceRollResults(it) }
    }

    // Add Action Button at the first level
    fun addActionButton(
        id: ButtonId,
        label: () -> String,
        icon: ActionIcon,
        onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FanOut(),
        subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        return topLevelMenu.addActionButton(
            id,
            label,
            icon,
            onClick,
            enabled,
            expandMode,
            subMenu
        )
    }

    fun <T: DieResult> addSimpleDiceButton(
        id: ButtonId,
        value: T,
        options: List<T>,
        parent: ActionWheelMenuItem,
        label: () -> String,
        onClick: (DieResult) -> Unit,
        enabled: Boolean,
        animate: Boolean
    ) {
        return topLevelMenu.addSimpleDiceButton(
            id,
            value = value,
            options = options,
            parent = parent,
            label = label,
            onClick = onClick,
            enabled = enabled,
            animate = animate,
        )
    }

    // Add dice button at the first level
    fun <T: DieResult> addDiceButtonWithSubmenu(
        id: ButtonId,
        diceId: DieId,
        diceValue: T,
        options: List<T>,
        enabled: Boolean = true,
        expandable: Boolean = true,
        onClick : (DieResult) -> Unit = { },
        animatingFrom: T? = null,
        onHover: (T?) -> String? = { null }
    ): DiceMenuWithSubmenuSelectorMenuItem<T> {
        return topLevelMenu.addDiceWithSubmenuButton(
            id,
            diceValue,
            options,
            enabled,
            expandable,
            onClick,
            animatingFrom,
            onHover
        )
    }

    // Add dice button at the first level
    fun <T: DieResult> addDiceButton(
        id: ButtonId,
        diceId: DieId,
        diceValue: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        onClick : (DieResult) -> Unit = { },
        animatingFrom: T? = null,
        onHover: (T?) -> String? = { null }
    ): DiceMenuWithPopupSelectorMenuItem<T> {
        return topLevelMenu.addDiceWithPopupButton(
            id,
            diceId,
            diceValue,
            options,
            preferLtr,
            enabled,
            expandable,
            onClick,
            animatingFrom,
            onHover
        )
    }

    fun addCoinButton(
        id: ButtonId,
        value: Coin,
        label: () -> String,
        enabled: Boolean,
        onClick: (Coin) -> Unit,
        startAnimationFrom: Coin? = null
    ): CoinMenuItem {
        return topLevelMenu.addCoinButton(
            id,
            value,
            label,
            enabled,
            onClick,
            startAnimationFrom,
        )
    }

    fun getDiceButton(index: Int): DiceMenuWithPopupSelectorMenuItem<*> {
        return menuItems.getOrNull(index)?.let {
            it as? DiceMenuWithPopupSelectorMenuItem<*> ?: error("$index is of the wrong type: ${it::class.simpleName}")
        } ?: error("$index is not in menu")
    }
}
