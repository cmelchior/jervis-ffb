package com.jervisffb.ui.game.dialogs.circle

/**
 * Enum describing the current state of a [ActionWheelMenuController] with regard to
 * animations.
 */
enum class ButtonLayoutMode {
    // The menu is in a steady state, i.e. no animations are running.
    STABLE,
    // Sub-menus are currently expanding out from a selected menu item.
    // I.e. going into a sub-menu.
    EXPAND,
    // Sub-menus are currently contracting back into their parent menu item.
    // I.e. going "up" a level in the hierarchy.
    CONTRACT
}
