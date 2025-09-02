package com.jervisffb.engine.rules

import com.jervisffb.engine.rules.bb2025.BB2025TeamActions
import com.jervisffb.engine.rules.builder.GameType
import kotlinx.serialization.Serializable

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 */
abstract class BB2025Rules : Rules(
    name = "Blood Bowl 2025 Rules",
    gameType = GameType.STANDARD,
    teamActions = BB2025TeamActions()
)

@Serializable
class StandardBB2025Rules : BB2025Rules() {
    override val name: String = "Blood Bowl 2025 Rules (Strict)"
}

