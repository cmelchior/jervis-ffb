package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.utils.assert

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
