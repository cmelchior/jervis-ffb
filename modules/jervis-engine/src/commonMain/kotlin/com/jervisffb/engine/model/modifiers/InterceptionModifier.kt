package com.jervisffb.engine.model.modifiers

enum class InterceptionModifier(override val modifier: Int, override val description: String) : DiceModifier {
    ACCURATE_PASS(-3, "Accurate Pass"),
    EXTRA_ARMS(1, "Extra Arms"),
    INACCURATE_PASS(-2, "Inaccurate Pass"),
    MARKED(-1, "Marked"),
    VERY_LONG_LEGS(2, "Very Long Legs")
}
