package com.jervisffb.ui.game.dialogs.wheel

/**
 * Enum describing how an Action Wheel will lay out menu items at a given level.
 * This will also take into account the parent menu item that created this level
 * and will place menus around the parent (if present).
 *
 * Some expansions require a `spread` value to be passed in. This defines how
 * much space around the circle is available for placing menu items. A spread of
 * 180 means items will occupy half the circle, 360 means the entire circle
 */
sealed interface MenuExpandMode {
    // This node does not have any submenus or does not support expanding into them
    data object None: MenuExpandMode

    // Only left/right. This assumes a maximum spread of 180 degrees.
    data class TwoWay(val direction: TwoDirection = TwoDirection.VERTICAL): MenuExpandMode

    // Distribute evenly all around the circle.
    data class FanOut(
        // Defines the maximum angle for the entire spread. 360 degrees is the entire circle.
        val spread: Float = 360f,
        // If `true` items will alternately change side from left to right, rather than being
        // laid out sequentially.
        val alternateSides: Boolean = false,
        // If `true`, the parent menu will not be considered when laying out items.
        val ignoreParent: Boolean = false

    ): MenuExpandMode

    /**
     * Cluster around the starting angle, using [ActionWheelMenuItem.stepAngle]
     * as the distance between items.
     */
    data class Compact(
        val spread: Float = 360f,
        // The angle between items in degrees
        val angleBetweenItemsDegrees: Float = 45f,
    ): MenuExpandMode

    /**
     * Enum that describes if a TwoWay menu should end being either horizontal
     * or vertical.
     */
    enum class TwoDirection {
        HORIZONTAL, VERTICAL
    }
}

