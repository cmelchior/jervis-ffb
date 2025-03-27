package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules

class SetBallLocation(val ball: Ball, val newLocation: FieldCoordinate) : Command {
    private lateinit var originalLocation: FieldCoordinate

    override fun execute(state: Game) {
        // This is only disabled because we need to call it from SetBallState (when a ball is being carried)
        // Probably we should look for a better way to be able to keep ball state in sync (right now it
        // has a lot of invariants. Maybe move everything into the helper methods in SetBallState)
        // assert(ball.state != BallState.CARRIED)
        val rules: Rules = state.rules
        this.originalLocation = ball.location
        ball.location = newLocation
        if (originalLocation.isOnField(rules)) {
            state.field[originalLocation].apply {
                balls.remove(ball)
                notifyUpdate()
            }
        }
        if (newLocation.isOnField(rules)) {
            state.field[newLocation].apply {
                balls.add(ball)
                notifyUpdate()
            }
        }
    }

    override fun undo(state: Game) {
        val rules = state.rules
        if (newLocation.isOnField(rules)) {
            state.field[newLocation].apply {
                balls.remove(this@SetBallLocation.ball)
                notifyUpdate()
            }
        }
        if (originalLocation.isOnField(rules)) {
            state.field[originalLocation].apply {
                balls.add(this@SetBallLocation.ball)
                notifyUpdate()
            }
        }
        ball.location = originalLocation
    }
}
