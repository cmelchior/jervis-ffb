package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class CatchModifier(override val modifier: Int, override val description: String) : DiceModifier {
    BOUNCING(-1, "Bouncing ball"),
    CONVERT_DEFLECTION(-1, "Deflection"),
    // This only applies to BB2020. This modifier was removed in BB2025
    DEVIATED(-1, "Deviated"),
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    DIVING_CATCH(1, "Diving Catch"),
    EXTRA_ARMS(1, "Extra Arms"),
    MARKED(-1, "Marked"),
    POURING_RAIN(-1, "Pouring Rain"),
    // This only applies to BB2020. This modifier was removed in BB2025
    SCATTERED(-1, "Scattered"),
    THROW_IN(-1, "Throw-in"),
}
