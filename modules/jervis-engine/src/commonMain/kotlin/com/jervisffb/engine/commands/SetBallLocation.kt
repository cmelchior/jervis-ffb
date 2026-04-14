package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules

class SetBallLocation(val ball: Ball, val newLocation: PitchCoordinate) : Command {
    private lateinit var originalLocation: PitchCoordinate

    override fun execute(state: Game) {
        // This is only disabled because we need to call it from SetBallState (when a ball is being carried)
        // Probably we should look for a better way to be able to keep ball state in sync (right now it
        // has a lot of invariants. Maybe move everything into the helper methods in SetBallState)
        // assert(ball.state != BallState.CARRIED)
        val rules: Rules = state.rules
        this.originalLocation = ball.coordinates
        ball.coordinates = newLocation
        if (originalLocation.isOnPitch(rules)) {
            state.pitch[originalLocation].apply {
                balls.remove(ball)
            }
        }
        if (newLocation.isOnPitch(rules)) {
            state.pitch[newLocation].apply {
                balls.add(ball)
            }
        }
    }

    override fun undo(state: Game) {
        val rules = state.rules
        if (newLocation.isOnPitch(rules)) {
            state.pitch[newLocation].apply {
                balls.remove(this@SetBallLocation.ball)
            }
        }
        if (originalLocation.isOnPitch(rules)) {
            state.pitch[originalLocation].apply {
                balls.add(this@SetBallLocation.ball)
            }
        }
        ball.coordinates = originalLocation
    }
}
