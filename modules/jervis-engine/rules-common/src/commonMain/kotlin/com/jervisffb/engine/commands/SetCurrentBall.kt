package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Sets an easy reference to the ball that is currently being handled.
 *
 * We store it as a reference on [Game] in order to reduce the number of
 * context updates that would otherwise happen when you have a ball that is
 * being scattered, bounced and caught.
 *
 * The procedure setting this, should also `null` it out again once it
 * is done with it.
 */
class SetCurrentBall(private val ball: Ball?) : Command {
    private var originalValue: Ball? = null

    override fun execute(state: Game) {
        originalValue = state.currentBallReference
        if (originalValue != null && ball != null) {
            INVALID_GAME_STATE("Attempting to override an already existing current ball")
        }
        state.currentBallReference = ball
        // No need to update state here since this is just an internal optimization.
    }

    override fun undo(state: Game) {
        state.currentBallReference = originalValue
        // No need to update state here since this is just an internal optimization.
    }
}
