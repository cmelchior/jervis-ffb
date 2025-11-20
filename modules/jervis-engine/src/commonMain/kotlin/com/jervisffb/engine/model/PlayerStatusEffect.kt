package com.jervisffb.engine.model

/**
 * Enum describing the various status effects a player can have.
 *
 * Developer's Commentary:
 * It looks like "Status Effect" has a specific meaning in BB 2025, but for
 * BB 2020, e.g., Rooted and Hypno-Gazed can also be considered "effects",
 * although the rulebook mostly focuses on what the effect does.
 */
enum class PlayerStatusEffect(val description: String) {
    // BB 2020
    HYPNO_GAZED("Hypno-Gazed"),

    // BB 2025
    ROOTED("Rooted"),
    DISTRACTED("Distracted"),
    CHOMPED("Chomped"),
    EYE_GOUGE("Eye Gouge"),
    DODGY_SNACK("Dodgy Snack")
}
