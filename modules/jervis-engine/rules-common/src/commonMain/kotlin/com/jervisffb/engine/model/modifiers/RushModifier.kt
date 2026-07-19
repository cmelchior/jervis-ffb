package com.jervisffb.engine.model.modifiers

/**
 * Modifiers that can affect a Rush roll.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.move.RushRoll]
 */
enum class RushModifier(override val modifier: Int, override val description: String) : DiceModifier {
    BLIZZARD(-1, "Blizzard"),
    MOLES_UNDER_THE_PITCH_AWAY(-1, "Moles under the Pitch (Away)"),
    MOLES_UNDER_THE_PITCH_HOME(-1, "Moles under the Pitch (Home)"),
}
