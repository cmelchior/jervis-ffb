package com.jervisffb.engine.rules.builder

/**
 * This enum describes which kind of Undo actions are allowed during a game.
 */
enum class UndoActionBehavior {
    NOT_ALLOWED, // No actions can be undone
    ONLY_NON_RANDOM_ACTIONS, // Only actions that don't require rolling dice can be undone.
    ALLOWED // All actions can be undone.
}
