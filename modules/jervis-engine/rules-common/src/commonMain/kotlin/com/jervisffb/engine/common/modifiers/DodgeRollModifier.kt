package com.jervisffb.engine.common.modifiers

import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.builder.GameVersion

/** We track Break Tackle modifiers as their own class as the value changes depending on the players' strength. */
data class BreakTackleModifier(val playerStrength: Int, val baseVersion: GameVersion): DiceModifier {
    override val modifier: Int = when (baseVersion) {
        GameVersion.BB2020 -> if (playerStrength > 4) 2 else 1
        GameVersion.BB2025 -> when {
            playerStrength <= 3 -> 1
            playerStrength == 4 -> 2
            playerStrength >= 5 -> 3
            else -> error("Unsupported player strength: $playerStrength")
        }
    }
    override val description: String = "Break Tackle"
}
