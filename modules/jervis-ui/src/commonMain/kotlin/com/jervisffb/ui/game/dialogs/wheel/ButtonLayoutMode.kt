package com.jervisffb.ui.game.dialogs.wheel

/**
 * Enum describing the current state of a [ActionWheelMenuController] with regard to
 * animations.
 */
enum class ButtonLayoutMode {
    CONTRACT_UNDO, // Hide current due to an Undo Event
    EXPAND_UNDO, // Expand current menu after reverting state due to Undo
    // The menu is in a steady state, i.e. no animations are running.
    STABLE,
    // No layout changes, just an extra delay
    DELAY,
    // We are animating rolls
    ANIMATING_ROLL,
    // Sub-menus are currently expanding out from a selected menu item.
    // The sub-menu consist of only new items.
    EXPEND_NEW_SUBMENU,
    // Sub-menus are currently contracting back into their parent menu item.
    // The sub-menu contained only new items.
    CONTRACT_NEW_SUBMENU,
    // Everything is animating out
    HIDE
}


fun ButtonLayoutMode?.isHiding(): Boolean {
    if (this == null) return true
    return (this == ButtonLayoutMode.HIDE || this == ButtonLayoutMode.CONTRACT_UNDO)
}
