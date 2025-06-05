package com.jervisffb.engine.model.modifiers

/**
 * Enumeration of Dodge Roll modifiers that are static.
 *
 * Also see [MarkedModifier],
 */
enum class DodgeRollModifier(override val modifier: Int, override val description: String) : DiceModifier {
    DIVING_TACKLE(-2, "Diving Tackle"),
    MARKED(-1, "Marked"),
    PREHENSILE_TAIL(-1, "Prehensile Tail"),
    TITCHY(1, "Titchy"),
    TWO_HEADS(1, "Two Heads"),
}

data class BreakTackleModifier(val playerStrength: Int): DiceModifier {
    override val modifier: Int = if (playerStrength > 4) 2 else 1
    override val description: String = "Break Tackle"
}
