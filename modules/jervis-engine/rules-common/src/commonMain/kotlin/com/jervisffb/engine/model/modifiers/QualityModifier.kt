package com.jervisffb.engine.model.modifiers

/**
 * Modifiers for Throw Team-mate Accuracy Roll.
 *
 * Quality Roll was the name used in BB2020.
 */
enum class QualityModifier(override val modifier: Int, override val description: String) : DiceModifier {
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    STRONG_ARM(1, "Strong Arm"),
    MARKED(-1, "Marked"),
    SHORT_PASS(-1, "Short Pass"),
    VERY_SUNNY(-1, "Very Sunny")
}
