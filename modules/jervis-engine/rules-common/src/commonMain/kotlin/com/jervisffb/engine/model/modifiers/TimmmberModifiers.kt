package com.jervisffb.engine.model.modifiers

enum class TimmmberModifiers(override val modifier: Int, override val description: String) : DiceModifier {
    // Timmm-ber helpers don't seem to have a name, so we just invented one here
    HELPING_HAND(1, "Helping Hand"),
}

data class HelpingHandsModifier(private val openPlayers: Int): DiceModifier {
    override val modifier: Int
        get() = openPlayers * TimmmberModifiers.HELPING_HAND.modifier
    override val description: String = TimmmberModifiers.HELPING_HAND.description
}
