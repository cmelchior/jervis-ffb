package com.jervisffb.engine.model.modifiers

enum class AccuracyModifier(override val modifier: Int, override val description: String) : DiceModifier {
    ACCURATE(1, "Accurate"),
    CANNONEER(1, "Cannoneer"),
    DISTURBING_PRESENCE(-1, "Disturbing Presence"),
    LONG_BOMB(-3, "Long Bomb"),
    LONG_PASS(-2, "Long Pass"),
    MARKED(-1, "Marked"),
    SHORT_PASS(-1, "Short Pass"),
    VERY_SUNNY(-1, "Very Sunny")
}
