package com.jervisffb.engine.model.modifiers

// Modifier representing multiple players with Disturbing Presence.
// `baseModifier` should be the modifier value for a single player
class DisturbingPresenceModifier(count: Int, baseModifier: DiceModifier) : DiceModifier {
    override val modifier: Int = baseModifier.modifier * count
    override val description: String = "Disturbing Presence"
}
