package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class CheeringFansModifiers(override val modifier: Int, override val description: String) : DiceModifier {
    CHEERLEADERS(1, "Cheerleaders"),
    TEAM_MASCOT(1, "Team Mascot")
}

class CheerleadersModifiers(count: Int): DiceModifier {
    override val modifier: Int = count * CheeringFansModifiers.CHEERLEADERS.modifier
    override val description: String = CheeringFansModifiers.CHEERLEADERS.description
}
