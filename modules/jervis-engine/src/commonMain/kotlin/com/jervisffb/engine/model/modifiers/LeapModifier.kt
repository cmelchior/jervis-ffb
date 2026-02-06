package com.jervisffb.engine.model.modifiers

enum class LeapModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    VERY_LONG_LEGS(1, "Very Long Legs"),
    LEAP(1, "Leap")
}
