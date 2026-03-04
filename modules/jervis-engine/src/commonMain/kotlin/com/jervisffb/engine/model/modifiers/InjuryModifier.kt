package com.jervisffb.engine.model.modifiers

enum class InjuryModifier(override val modifier: Int, override val description: String) : DiceModifier {
    DIRTY_PLAYER(1, "Dirty Player"),
    STUNTY(1, "Stunty"),
}

data class MightyBlowInjuryModifier(override val modifier: Int = 1) : DiceModifier {
    override val description: String = "Mighty Blow"
}


