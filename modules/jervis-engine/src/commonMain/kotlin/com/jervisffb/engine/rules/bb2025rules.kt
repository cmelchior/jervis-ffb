package com.jervisffb.engine.rules

import com.jervisffb.engine.rules.builder.GameType

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 */
abstract class BB2025Rules : Rules(
    name = "Blood Bowl 2025 Rules",
    gameType = GameType.STANDARD
)
