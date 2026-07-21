package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class InterceptionModifier(override val modifier: Int, override val description: String) : DiceModifier {
    ACCURATE_PASS(-3, "Accurate Pass"),
    DISTURBING__PRESENCE(-1, "Disturbing Present"),
    EXTRA_ARMS(1, "Extra Arms"),
    INACCURATE_PASS(-2, "Inaccurate Pass"),
    MARKED(-1, "Marked"),
    VERY_LONG_LEGS(2, "Very Long Legs"),
    STUNTY(-1, "Stunty")
}

