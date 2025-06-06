package com.jervisffb.engine.model.modifiers

enum class PassingInterferenceModifier(override val modifier: Int, override val description: String) : DiceModifier {
    ACCURATE_PASS(-3, "Accurate Pass"),
    INACCURATE_PASS(-2, "Inaccurate Pass"),
    WILDLY_INACCURATE_PASS(-1, "Wildly Inaccurate Pass"),
    MARKED(-1, "Marked"),
}
