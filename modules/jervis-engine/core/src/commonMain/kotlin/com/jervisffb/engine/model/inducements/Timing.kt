package com.jervisffb.engine.model.inducements

/**
 * This enum represents when Special Play Cards, Spells, Desperate Measures, and
 * other inducement effects can trigger.
 *
 * Developer's Commentary:
 * Having buckets allows the rule engine to correctly handle multiple effects
 * that might trigger at the same time. For this reason, [SPECIAL] should also
 * be avoided unless the trigger is very specific. In most cases, it is better
 * to add a new bucket.
 */
enum class Timing {
    BEFORE_FIRST_SETUP, // Trigger before either teams are set up at the beginning of the game.
    BEFORE_SETUP,
    AFTER_SETUP, // Trigger after both teams are set up, but before Kick-off starts.
    ACTIVATE_PLAYER, // When a player is activated and about to declare actions.
    PERFORM_PASS_ACTION,
    AFTER_TURNOVER,
    ENTER_TACKLEZONE,
    END_OF_ANY_TURN,
    END_OF_OWN_TURN, // After own turn "has ended(?)"
    END_OF_OPPONENT_TURN, // After "opponent's turn has ended(?)"

    START_OF_DRIVE,
    START_OF_OWN_TURN, // Before any players are activated
    START_OF_OPPONENT_TURN, // Before any players are activated
    END_OF_DRIVE, // During step 3 in End Of Drive Sequence
    SPECIAL // The timing effect cannot easily be handled generically and needs to be manually checked
}
