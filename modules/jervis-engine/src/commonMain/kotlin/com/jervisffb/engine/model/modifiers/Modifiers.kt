package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.utils.assert


/**
 * Generic interface for something that modifies the value of a dice roll.
 */
interface DiceModifier {
    val modifier: Int
    val description: String
}

// Modifiers added due to a square being marked by players from the opposite team.
// I.e. the DiceModifier is always negative.
class MarkedModifier(markingPlayers: Int, type: DiceModifier) : DiceModifier {
    init {
        assert(markingPlayers >= 0) { "Marking players must be positive: $markingPlayers" }
        assert(type.modifier < 0) { "Type modifier must be negative: ${type.modifier}" }
    }
    override val modifier: Int = markingPlayers * type.modifier
    override val description: String = "Marked"
}

class NigglingInjuryModifier(val player: Player) : DiceModifier {
    override val modifier: Int = player.nigglingInjuries * -1
    override val description: String = "Niggling Injury"
}



