package com.jervisffb.ui.game.dialogs.circle

/**
 * Enum describing how an Action Wheel will lay out menu items at a given level.
 * This will also take into account the parent menu item that created this level
 * and will place menus around the parent (if present).
 */
enum class MenuExpandMode {
    NONE,
    // Only left/right
    TWO_WAY,
    // Distribute evenly all around the circle
    FAN_OUT,
    // Cluster around the starting angle
    COMPACT,
}
