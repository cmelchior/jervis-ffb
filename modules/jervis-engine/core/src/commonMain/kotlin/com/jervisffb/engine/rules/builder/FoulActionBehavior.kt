package com.jervisffb.engine.rules.builder

/**
 * This enum describes how the foul target is selected during a Foul action.
 */
enum class FoulActionBehavior {
    BB2025, // Select the target just before rolling for the foul
    BB2020, // Select the target when declaring the foul action
}
