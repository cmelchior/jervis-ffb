package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Player

class NigglingInjuryModifier(val player: Player) : DiceModifier {
    override val modifier: Int = player.nigglingInjuries * -1
    override val description: String = "Niggling Injury"
}
