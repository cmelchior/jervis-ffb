package com.jervisffb.engine.model.modifiers

enum class LandingModifier(override val modifier: Int, override val description: String) : DiceModifier {
    // Only used in BB2025
    SUPERB(0, "Superb Throw"),
    SUBPAR(-1, "Subpar Throw"),

    // Only used in BB2020
    SUCCESSFUL(-1, "Successful Throw"),
    TERRIBLE(-2, "Terrible Throw"),

    // Common
    FUMBLED(-1, "Fumbled Throw"),
    MARKED(-1, "Marked"),
}
