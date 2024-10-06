package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Player


/**
 * Generic interface for something that modifies the value of a dice roll.
 */
interface DiceModifier {
    val modifier: Int
    val description: String
}

// Modifiers added due to a square being marked by players from the opposite team
data class MarkedModifier(override val modifier: Int) : DiceModifier {
    override val description: String = "Marked"
}

// Modifiers added du to offensive assists during a block
data class OffensiveAssistModifier(
    override val modifier: Int,
    override val description: String = "Offensive Assists"
) : DiceModifier

// Modifiers added due to defensive assists during a block
data class DefensiveAssistsModifier(
    override val modifier: Int,
    override val description: String = "Defensive Assists"
) : DiceModifier

class NigglingInjuryModifier(val player: Player) : DiceModifier {
    override val modifier: Int = player.nigglingInjuries * -1
    override val description: String = "Niggling Injury"
}



