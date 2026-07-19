package com.jervisffb.engine.reports

/**
 * This enum represents the different categories we can put game logs into.
 * This will help us filter them better, based on the area of responsibility.
 */
enum class LogCategory {
    ACTIONS,
    STATE_MACHINE,
    GAME_PROGRESS,
    DICE_ROLL,
    BOARD,
}
