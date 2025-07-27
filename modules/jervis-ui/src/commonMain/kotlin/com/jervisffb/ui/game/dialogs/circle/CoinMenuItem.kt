package com.jervisffb.ui.game.dialogs.circle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jervisffb.engine.model.Coin

/**
 * Class representing a "coin" menu item in the action wheel.
 */
class CoinMenuItem(
    val value: Coin,
    override val parent: ActionWheelMenuItem?,
    override val label: () -> String,
    override val enabled: Boolean,
    val onClick: (Coin) -> Unit,
    private val startAnimationFrom: Coin? = null
): ActionWheelMenuItem() {
    override val expandMode: MenuExpandMode = MenuExpandMode.NONE
    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
    var animatingFrom: Coin? by mutableStateOf(startAnimationFrom)
    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
        // Do nothing
    }
}
