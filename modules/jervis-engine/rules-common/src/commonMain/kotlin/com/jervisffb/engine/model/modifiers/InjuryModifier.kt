package com.jervisffb.engine.model.modifiers

enum class InjuryModifier(override val modifier: Int, override val description: String) : DiceModifier {
    ARM_BAR(1, "Arm Bar"),
    DIRTY_PLAYER(1, "Dirty Player"),
    STUNTY(1, "Stunty"),
    LETHAL_FLIGHT(1, "Lethal Flight"),
}

data class MightyBlowInjuryModifier(override val modifier: Int = 1) : DiceModifier {
    override val description: String = "Mighty Blow"
}


