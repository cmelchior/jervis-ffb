package com.jervisffb.engine.model.modifiers

enum class TimmmberModifiers(override val modifier: Int, override val description: String) : DiceModifier {
    // Timmm-ber helpers don't seem to have a name, so we just invented one here
    HELPING_HAND(1, "Helping Hand"),
}

data class HelpingHandsModifier(override val modifier: Int): DiceModifier {
    override val description: String = TimmmberModifiers.HELPING_HAND.description
}
