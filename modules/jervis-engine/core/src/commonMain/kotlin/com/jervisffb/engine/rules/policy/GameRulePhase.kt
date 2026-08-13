package com.jervisffb.engine.rules.policy

/**
 * Identifies which part of a game's lifecycle is asking a rule policy for a
 * decision.
 */
enum class GameRulePhase {
    INITIAL_ACTIONS, // Used whe applying initial actions in a GameEngineController.
    LIVE, // Anything after initial actions have been applied.
}
