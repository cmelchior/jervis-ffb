package com.jervisffb.engine.rules.policy

import com.jervisffb.engine.model.Game

/**
 * Marker interface for context classes used by subclasses of [GameRulePolicy].
 *
 * This is not used for anything useful right now but just makes it easy to find
 * them if needed.
 */
interface GameRulePolicyContext {
    // Current game state. This is as a mutable reference but should not be
    // modified.
    val state: Game
}
