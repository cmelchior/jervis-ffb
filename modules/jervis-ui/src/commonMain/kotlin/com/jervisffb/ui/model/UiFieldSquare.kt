package com.jervisffb.ui.model

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.ui.view.ContextMenuOption

/**
 * Represents all information needed to render a single square
 */
class UiFieldSquare(
    // "Static state", i.e. state that is not related to any given action
    val model: FieldSquare,
    val isBallOnGround: Boolean = false,
    val isBallExiting: Boolean = false,
    val isBallCarried: Boolean = false,
    val player: UiPlayer? = null,
) {
    // State that are related to actions
    var selectableDirection: Direction? = null // Show selectable direction arrow (i.e. with hover effect)
    var directionSelected: Direction? = null // Show a direction arrow in its selected state
    var dice: Int = 0 // Show block dice decorator
    var requiresRoll: Boolean = false // onSelected is not-null but will result in dice being rolled
    var onSelected: (() -> Unit)? = null // Action if square is selected
    var onMenuHidden: (() -> Unit?)? = null // Action if the context menu is hidden
    var showContextMenu: Boolean = false // The context menu is automatically opened
    var contextMenuOptions: MutableList<ContextMenuOption> = mutableListOf() // The options inside the context menu
}



