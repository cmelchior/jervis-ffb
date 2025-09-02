package com.jervisffb.engine.model

/**
 * Enum describing the various status effects a player can have.
 *
 * **Developer's Commentary**
 * It looks like "Status Effect" has a specific meaning in BB 2025, but for
 * BB 2020, e.g., Rooted and Hypno-Gazed can also be considered "effects",
 * although the rulebook mostly focus on what the effect does.
 */
enum class PlayerStatusEffect {
    // BB 2020
    ROOTED,
    HYPNO_GAZED,

    // BB 2025
    DISTRACTED,
    CHOMPED,
}
