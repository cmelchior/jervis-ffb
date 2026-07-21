package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class CasualtyModifier(override val modifier: Int, override val description: String) : DiceModifier {
    DECAY(1, "Decay"),
}
