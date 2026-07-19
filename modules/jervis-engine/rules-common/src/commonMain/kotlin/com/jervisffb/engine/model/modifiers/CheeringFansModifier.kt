package com.jervisffb.engine.model.modifiers

enum class CheeringFansModifiers(override val modifier: Int, override val description: String) : DiceModifier {
    CHEERLEADERS(1, "Cheerleaders"),
    TEAM_MASCOT(1, "Team Mascot")
}

class CheerleadersModifiers(count: Int): DiceModifier {
    override val modifier: Int = count * CheeringFansModifiers.CHEERLEADERS.modifier
    override val description: String = CheeringFansModifiers.CHEERLEADERS.description
}
