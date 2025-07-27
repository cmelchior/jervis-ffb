package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.DieId
import com.jervisffb.ui.game.icons.ActionIcon

/**
 * Main entry point into controlling either the "top" or "bottom" menus of the
 * action wheel.
 */
class ActionWheelMenuController(
    viewModel: ActionWheelViewModel,
    startingAngle: Float,
    expandMode: MenuExpandMode
) {
    val topLevelMenu = TopLevelMenuItem(viewModel, startingAngle, expandMode = expandMode)
    val menuItems = topLevelMenu.subMenu

    /**
     * Returns the values of all dice as a [DiceRollResults] game action.
     */
    fun getDiceResults(): DiceRollResults {
        return menuItems
            .filterIsInstance<DiceMenuItem<*>>()
            .map { it.value }
            .let { DiceRollResults(it) }
    }

    // Add Action Button at the first level
    fun addActionButton(
        label: () -> String,
        icon: ActionIcon,
        onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
        subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        return topLevelMenu.addActionButton(
            label,
            icon,
            onClick,
            enabled,
            expandMode,
            subMenu
        )
    }

    // Add dice button at the first level
    fun <T: DieResult> addDiceButton(
        id: DieId,
        diceValue: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        animatingFrom: T? = null,
        onHover: (T?) -> Unit = { }
    ): DiceMenuItem<T> {
        return topLevelMenu.addDiceButton(
            id,
            diceValue,
            options,
            preferLtr,
            enabled,
            expandable,
            animatingFrom,
            onHover
        )
    }

    fun addCoinButton(
        value: Coin,
        label: () -> String,
        enabled: Boolean,
        onClick: (Coin) -> Unit,
        startAnimationFrom: Coin? = null
    ): CoinMenuItem {
        return topLevelMenu.addCoinButton(
            value,
            label,
            enabled,
            onClick,
            startAnimationFrom,
        )
    }

    fun getDiceButton(index: Int): DiceMenuItem<*> {
        return menuItems.getOrNull(index)?.let {
            it as? DiceMenuItem<*> ?: error("$index is of the wrong type: ${it::class.simpleName}")
        } ?: error("$index is not in menu")
    }
}
