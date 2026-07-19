package com.jervisffb.engine.rules.builder

/**
 * This enum describes who is responsible for rolling any dice as part of a
 * game.
 */
enum class DiceRollOwner {
    // The server logic is responsible for rolling the dice.
    ROLL_ON_SERVER,
    // Client is responsible for controlling the dice roll by either
    // letting the user choose the result or rolling behind the users
    // back.
    ROLL_ON_CLIENT
}
