package com.jervisffb.engine.rules.builder

enum class DiceRollOwner {
    // The server logic is responsible for rolling the dice.
    ROLL_ON_SERVER,
    // Client is responsible for controlling the dice roll by either
    // letting the user choose the result or rolling behind the users
    // back.
    ROLL_ON_CLIENT
}

enum class UndoActionBehavior {
    NOT_ALLOWED,
    ONLY_NON_RANDOM_ACTIONS,
    ALLOWED
}
enum class FoulActionBehavior {
    STRICT, // Select target when starting the foul action
    FUMBBL, // Select the foul player, just before rolling for the foul
}

enum class KickingPlayerBehavior {
    STRICT, // Player should be selected by the Client
    FUMBBL // Player is selected automatically by the server.
}
