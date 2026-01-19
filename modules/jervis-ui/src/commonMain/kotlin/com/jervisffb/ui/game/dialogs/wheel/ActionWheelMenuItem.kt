package com.jervisffb.ui.game.dialogs.wheel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.DieId
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.icons.ActionIcon
import kotlin.math.ceil

/**
 * This class represents a menu item in the Action Wheel.
 * A menu item has two "modes": Expanded and Contracted.
 *
 * Expanded:
 * It has been selected and is considered the "primary" menu item. This means
 * the item is at the "center" and its submenus are spread out around it.
 *
 * Contracted:
 * Its parent is currently expanded and the "primary". This means this menu item
 * is laid out according to the parents [expandMode].
 */
sealed class ActionWheelMenuItem {
    abstract val id: ButtonId
    abstract var label: () -> String
    abstract val parent: ActionWheelMenuItem?
    abstract var enabled: Boolean
    abstract val expandMode: MenuExpandMode
    abstract val subMenu: SnapshotStateList<ActionWheelMenuItem>
    // Only used when this menu item is contracting as a submenu
    open val contractMode: ButtonLayoutMode = ButtonLayoutMode.CONTRACT_NEW_SUBMENU

    // Note: Angles in this implementation are in degrees, not radians, unless
    // otherwise noted. This is considered the "starting point" for all
    // relative-based calculations. 90° = bottom of the screen.

    // TODO How is this different from `startMainAngle`?
    open val defaultStartAngle: Float
        get() = parent?.defaultStartAngle ?: 0f

    // Angle item should move to when promoted to "main item". +90 is bottom-center.
    open val startMainAngle = 90f
    // Which angle to put this menu in when it is displayed as a "sub item"
    // These are normally calculated by calling `recalculateSubMenuAngles()`
    var startSubAngle = 0f
    // Only used in `COMPACT` mode and defines the distance in degrees between
    // each sub item.
    val stepAngle = 45f

    abstract fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean)

    private fun recalculateSubMenuAngles() {
        if (subMenu.isEmpty()) return
        when (val mode = expandMode) {
            MenuExpandMode.None -> error("Not supported")
            MenuExpandMode.TwoWay -> {
                subMenu.forEachIndexed { index, item ->
                    when (index) {
                        0 -> item.startSubAngle = 180f
                        1 -> item.startSubAngle = 0f
                        else -> error("Too many item: ${subMenu.size}")
                    }
                }
            }
            is MenuExpandMode.FanOut -> {
                // If no parent, evenly distribute items in the ring.
                // For an even number of items, one will always be on the `defaultStartAngle`
                // For an odd number of items, `defaultStartAngle` will be in the middle between two items.
                // directly on `centerAngle`
                if (parent == null || mode.ignoreParent) {
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = (mode.spread / subMenu.size) * i + defaultStartAngle
                    }
                } else {
                    // Spread items out from "primary", taking up as much space as allowed.
                    val spread = mode.spread // Maximum spread of the menu
                    val step = if (subMenu.size > 1) spread / (subMenu.size - 1) else 0f
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = defaultStartAngle + (spread / 2) + (step * i)
                    }
                }
            }
            is MenuExpandMode.Compact -> {
                // Clump menu items together at `centerAngle`. For an even number of menu items
                // This means none of them will be directly on `centerAngle`.
                val offset = if (subMenu.size % 2 == 0) stepAngle / 2f else 0f
                val parentModifier = if (parent == null) 0 else 1
                subMenu.forEachIndexed { index, item ->
                    val direction = if (index % 2 == 1) -1 else 1
                    val magnitude = ceil((index + parentModifier) / 2.0).toFloat()
                    item.startSubAngle = (defaultStartAngle + offset + direction * magnitude * stepAngle)
                }
            }
        }
    }

    fun addActionButton(
        id: ButtonId,
        label: () -> String,
        icon: ActionIcon,
        onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FanOut(),
        subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        val button = ActionMenuItem(
            id,
            this,
            label,
            icon,
            onClick,
            enabled,
            expandMode,
            subMenu
        )
        this.subMenu.add(button)
        recalculateSubMenuAngles()
        return button
    }

    fun <T: DieResult> addDiceWithPopupButton(
        id: ButtonId,
        diceId: DieId,
        value: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        onClick: (DieResult) -> Unit = { },
        animateValueFrom: T? = null,
        onHover: (T?) -> String?,
    ): DiceMenuWithPopupSelectorMenuItem<T> {
        val button = DiceMenuWithPopupSelectorMenuItem(
            id,
            this,
            diceId,
            value,
            options,
            preferLtr,
            enabled,
            expandable,
            onClick,
            animateValueFrom,
            onHover
        )
        subMenu.add(button)
        recalculateSubMenuAngles()
        return button
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
        val button = SimpleDiceMenuItem(
            id = id,
            value = value,
            options = options,
            parent = parent,
            label = label,
            onClick = onClick,
            enabled = enabled,
            startAnimationFrom = if (animate) options.random() else null
        )
        subMenu.add(button)
        recalculateSubMenuAngles()
    }

    fun <T: DieResult> addDiceWithSubmenuButton(
        id: ButtonId, // id of the final value
        value: T, // current value
        options: List<T>, // list of options
        enabled: Boolean = true,
        expandable: Boolean = true,
        onClick: (DieResult) -> Unit = { },
        animateValueFrom: T? = null,
        onHover: (T?) -> String?,
    ): DiceMenuWithSubmenuSelectorMenuItem<T> {
        val button = DiceMenuWithSubmenuSelectorMenuItem(
            id = id,
            parent = this,
            selectedDice = value,
            options = options,
            enabled = enabled,
            expandable = expandable,
            onClick = onClick,
            startAnimationFrom = animateValueFrom,
            onHover = onHover
        )
        subMenu.add(button)
        recalculateSubMenuAngles()
        return button
    }

    fun addCoinButton(
        id: ButtonId,
        value: Coin,
        label: () -> String,
        enabled: Boolean,
        onClick: (Coin) -> Unit,
        startAnimationFrom: Coin? = null
    ): CoinMenuItem {
        val button = CoinMenuItem(
            id,
            value,
            this,
            label,
            enabled,
            onClick,
            startAnimationFrom
        )
        subMenu.add(button)
        recalculateSubMenuAngles()
        return button
    }
}
