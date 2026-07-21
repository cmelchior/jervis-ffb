package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier

enum class JumpUpModifier(override val modifier: Int, override val description: String) : DiceModifier {
    JUMP_UP(1, "Jump Up"),
}
