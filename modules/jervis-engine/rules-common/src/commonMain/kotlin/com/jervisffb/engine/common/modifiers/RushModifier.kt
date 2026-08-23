package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

/**
 * Modifiers that can affect a Rush roll.
 */
enum class RushModifier(override val modifier: Int, override val description: String) : DiceModifier {
    BLIZZARD(-1, "Blizzard"),
    MOLES_UNDER_THE_PITCH_AWAY(-1, "Moles under the Pitch (Away)"),
    MOLES_UNDER_THE_PITCH_HOME(-1, "Moles under the Pitch (Home)"),
}
