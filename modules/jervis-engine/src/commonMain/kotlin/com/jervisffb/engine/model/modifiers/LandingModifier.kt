package com.jervisffb.engine.model.modifiers

enum class LandingModifier(override val modifier: Int, override val description: String) : DiceModifier {
    SUPERB_THROW(0, "Superb Throw"),
    SUCCESSFUL_THROW(-1, "Successful Throw"),
    FUMBLED_THROW(-1, "Fumbled Throw"),
    TERRIBLE_THROW(-2, "Terrible Throw"),
    MARKED(-1, "Marked"),
}
