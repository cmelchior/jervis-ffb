package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class ArgueTheCallRollModifier(
    override val modifier: Int,
    override val description: String
) : DiceModifier {
    I_DID_NOT_SEE_A_THING(1, "I didn't see a thing"), // Biased Referee Inducement
}

