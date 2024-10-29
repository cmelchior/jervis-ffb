package com.jervisffb.ui.model

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.ui.view.ContextMenuOption

/**
 * Represents all information needed to render a single square
 */
data class UiFieldSquare(
    // "Static state", i.e. state that is not related to any given action
    val model: FieldSquare,
    val isBallOnGround: Boolean = false,
    val isBallExiting: Boolean = false,
    val isBallCarried: Boolean = false,
    val player: UiPlayer? = null,

    // State that are related to actions
    val selectableDirection: Direction? = null, // Show selectable direction arrow (i.e. with hover effect)
    val directionSelected: Direction? = null, // Show a direction arrow in its selected state
    val dice: Int = 0, // Show block dice decorator
    val requiresRoll: Boolean = false, // onSelected is not-null but will result in dice being rolled
    val onSelected: (() -> Unit)? = null, // Action if square is selected
    val onMenuHidden: (() -> Unit?)? = null, // Action if the context menu is hidden
    val showContextMenu: Boolean = false, // The context menu is automatically opened
    val contextMenuOptions: MutableList<ContextMenuOption> = mutableListOf() // The options inside the context menu
) {

    fun copyAddContextMenu(item: ContextMenuOption): UiFieldSquare {
        return this.copy().also { contextMenuOptions += item }
    }
}


