package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Player

class OffensiveTentaclesModifier(player: Player) : DiceModifier {
    override val modifier: Int = player.strength
    override val description: String = "Offensive Strength"
}

class DefensiveTentaclesModifier(player: Player) : DiceModifier {
    override val modifier: Int = player.strength * -1
    override val description: String = "Defensive Strength"
}
