package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.reports.ReportStartingExtraTime.id
import com.jervisffb.ui.game.icons.ActionIcon
import kotlin.math.ceil

/**
 * This class represents a menu item in the Action Wheel.
 * A menu item has two "modes": Expanded and Contracted.
 *
 * Expanded: It has been selected and is considered the "primary" menu item. This
 *           means the item is at the "center" and its submenus are spread out
 *           around it.
 * Contracted: Its parent is currently expanded and the "primary". This means this
 *             menu item is laid out according to the parents [expandMode].
 */
sealed class ActionWheelMenuItem {
    abstract val label: () -> String
    abstract val parent: ActionWheelMenuItem?
    abstract val enabled: Boolean
    abstract val expandMode: MenuExpandMode
    abstract val subMenu: SnapshotStateList<ActionWheelMenuItem>

    // Note: Angles in this implementation are in degrees, not radians, unless
    // otherwise noted. This is considered the "starting point" for all
    // relative-based calculations. 90° = bottom of the screen.

    open val defaultStartAngle: Float
        get() = parent?.defaultStartAngle ?: 0f

    val startMainAngle = 90f // Angle item should move to when promoted to "main item"
    var startSubAngle = 0f // Which angle to put this menu in when it is displayed as a "sub item"
    val stepAngle = 45f

    abstract fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean)

    private fun recalculateSubMenuAngles() {
        if (subMenu.isEmpty()) return
        when (expandMode) {
            MenuExpandMode.NONE -> error("Not supported")
            MenuExpandMode.TWO_WAY -> {
                subMenu.forEachIndexed { index, item ->
                    when (index) {
                        0 -> {
                            item.startSubAngle = 180f
                        }
                        1 -> {
                            item.startSubAngle = 0f
                        }
                        else -> error("Too many item: ${subMenu.size}")
                    }
                }
            }
            MenuExpandMode.FAN_OUT -> {
                // Evenly distribute items in the ring. This means there is always one menu item
                // directly on `centerAngle`
                if (parent == null) {
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = (360f / subMenu.size) * i + defaultStartAngle
                    }
                } else {
                    val spread = 180f
                    val step = if (subMenu.size > 1) spread / (subMenu.size - 1) else 0f
                    subMenu.forEachIndexed { i, item ->
                        item.startSubAngle = defaultStartAngle + (spread / 2) + (step * i)
                    }
                }
            }
            MenuExpandMode.COMPACT -> {
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
        label: () -> String,
        icon: ActionIcon,
        onClick: (parent: ActionWheelMenuItem?, button: ActionWheelMenuItem) -> Unit,
        enabled: Boolean = true,
        expandMode: MenuExpandMode = MenuExpandMode.FAN_OUT,
        subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    ): ActionMenuItem {
        val button = ActionMenuItem(
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

    fun <T: DieResult> addDiceButton(
        id: DieId,
        value: T,
        options: List<T>,
        preferLtr: Boolean = true,
        enabled: Boolean = true,
        expandable: Boolean = true,
        onClick: (DieResult) -> Unit = { },
        animateValueFrom: T? = null,
        onHover: (T?) -> Unit,
    ): DiceMenuItem<T> {
        val button = DiceMenuItem(
            this,
            id,
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

    fun addCoinButton(
        value: Coin,
        label: () -> String,
        enabled: Boolean,
        onClick: (Coin) -> Unit,
        startAnimationFrom: Coin? = null
    ): CoinMenuItem {
        val button = CoinMenuItem(
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
