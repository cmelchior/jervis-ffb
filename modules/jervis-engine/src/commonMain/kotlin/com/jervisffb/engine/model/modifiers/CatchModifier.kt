package com.jervisffb.engine.model.modifiers

enum class CatchModifier(override val modifier: Int, override val description: String) : DiceModifier {
    CONVERT_DEFLECTION(-1, "Deflection"),
    BOUNCING(-1, "Bouncing ball"),
    THROW_IN(-1, "Throw-in"),
    SCATTERED(-1, "Scattered"),
    DEVIATED(-1, "Deviated"),
    MARKED(-1, "Marked"),
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    POURING_RAIN(-1, "Pouring Rain"),
}
