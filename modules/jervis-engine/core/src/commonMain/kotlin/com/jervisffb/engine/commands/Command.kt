package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

/**
 * The main interface for manipulating [Game] state. All changes to game state
 * must happen through this interface as it enables us to move forward and
 * backwards through the game state seamlessly.
 *
 * Multiple commands can be combined using a [CompositeCommand]. See
 * [buildCompositeCommand] or [compositeCommandOf] for easy factory builders.
 */
interface Command {
    /**
     * Apply this change to the current game state. Moving the state "forward".
     */
    fun execute(state: Game)

    /**
     * Undo the change represented by this command, moving the state "backward".
     * This method should only be called if [execute] has been called first at
     * some point in time.
     */
    fun undo(state: Game)

    /**
     * Combine this command with another. This command will be executed first.
     */
    operator fun plus(other: Command): Command {
        return compositeCommandOf(this, other)
    }
}
