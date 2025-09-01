package com.jervisffb.engine.rules.common.skills

// When does the "used" state reset?
enum class Duration {
    IMMEDIATE, // The effect expires immediately.
    START_OF_ACTIVATION, // The effect expires when the player is activated
    END_OF_ACTIVATION, // The effect expires at the end of the current players activation
    END_OF_ACTION, // The effect expires at the end of the action, this also includes subactions, like the Block part of a Blitz.
    END_OF_TURN, // The effect expires at the end of the current teams turn.
    END_OF_DRIVE, // The effect expires at the end of the current drive
    END_OF_HALF, // The effect expires at the end of the current half
    END_OF_GAME, // The effect lasts for the entire game, but doesn't carry over to the next game
    SPECIAL, // The duration of this effect is too hard to put into a bucket and must be handled manually.
    STANDING_UP, // The effect expires when the player is going from prone to standing up.
    PERMANENT, // The effect is a permanent change to the team.
}
