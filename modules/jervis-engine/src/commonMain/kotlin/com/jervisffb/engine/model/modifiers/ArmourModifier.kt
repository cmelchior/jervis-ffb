package com.jervisffb.engine.model.modifiers

enum class ArmourModifier(override val modifier: Int, override val description: String) : DiceModifier {
    DIRTY_PLAYER(1, "Dirty Player"),
    MARKED(1, "Marked"),
}

data class MightyBlowModifier(override val modifier: Int = 1) : DiceModifier {
    override val description: String = "Mighty Blow"
}

// Modifiers added du to offensive assists during a foul
data class OffensiveAssistArmourModifier(
    val count: Int,
    override val description: String = "Offensive Assists"
) : DiceModifier {
    override val modifier: Int = count * ArmourModifier.MARKED.modifier
}

// Modifiers added due to defensive assists during a foul
data class DefensiveAssistsArmourModifier(
    val count: Int,
    override val description: String = "Defensive Assists"
) : DiceModifier {
    override val modifier: Int = count * ArmourModifier.MARKED.modifier * -1
}


