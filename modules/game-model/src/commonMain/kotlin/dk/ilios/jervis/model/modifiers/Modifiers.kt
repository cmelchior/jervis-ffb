package dk.ilios.jervis.model.modifiers

import dk.ilios.jervis.model.Player


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

class StatModifier private constructor(val modifier: Int, val type: Type) {
    enum class Type {
        AV, MA, PA, AG, ST
    }
    companion object {
        fun AV(modifier: Int) = StatModifier(modifier, Type.AV)
        fun MA(modifier: Int) = StatModifier(modifier, Type.MA)
        fun PA(modifier: Int) = StatModifier(modifier, Type.PA)
        fun AG(modifier: Int) = StatModifier(modifier, Type.AG)
        fun ST(modifier: Int) = StatModifier(modifier, Type.ST)
    }
}


