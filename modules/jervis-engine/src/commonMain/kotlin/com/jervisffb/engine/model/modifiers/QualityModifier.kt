package com.jervisffb.engine.model.modifiers

enum class QualityModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    SHORT_PASS(-1, "Short Pass"),
    VERY_SUNNY(-1, "Very Sunny")
}

