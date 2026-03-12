package com.jervisffb.engine.model.modifiers

enum class CatchModifier(override val modifier: Int, override val description: String) : DiceModifier {
    BOUNCING(-1, "Bouncing ball"),
    CONVERT_DEFLECTION(-1, "Deflection"),
    DEVIATED(-1, "Deviated"),
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    DIVING_CATCH(1, "Diving Catch"),
    EXTRA_ARMS(1, "Extra Arms"),
    MARKED(-1, "Marked"),
    POURING_RAIN(-1, "Pouring Rain"),
    SCATTERED(-1, "Scattered"),
    THROW_IN(-1, "Throw-in"),
}
