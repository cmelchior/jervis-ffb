package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class JumpModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    VERY_LONG_LEGS(1, "Very Long Legs"),
    DIVING_TACKLE(-2, "Diving Tackle"),
}
