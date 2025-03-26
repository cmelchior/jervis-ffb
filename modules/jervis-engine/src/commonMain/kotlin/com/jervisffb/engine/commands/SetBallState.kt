package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

class SetBallState private constructor(
    private val ball: Ball,
    private val ballState: BallState,
    private val carriedBy: Player? = null,
    private val exitLocation: FieldCoordinate? = null,
) : Command {
    private lateinit var originalState: BallState
    private var originalCarriedBy: Player? = null
    private var originalExit: FieldCoordinate? = null
    private var originalLocation: FieldCoordinate? = null

    companion object {
        fun accurateThrow(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.ACCURATE_THROW,
            carriedBy = null,
            exitLocation = null
        )

        fun inAir(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.IN_AIR,
            carriedBy = null,
            exitLocation = null
        )

        fun carried(ball: Ball, player: Player): Command = SetBallState(
            ball = ball,
            ballState = BallState.CARRIED,
            carriedBy = player,
            exitLocation = null
        )

        fun onGround(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.ON_GROUND,
            carriedBy = null,
            exitLocation = null
        )

        fun deviating(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.DEVIATING,
            carriedBy = null,
            exitLocation = null
        )

        fun bouncing(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.BOUNCING,
            carriedBy = null,
            exitLocation = null,
        )

        fun scattered(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.SCATTERED,
            carriedBy = null,
            exitLocation = null
        )

        fun outOfBounds(ball: Ball, exit: FieldCoordinate): Command = SetBallState(
            ball = ball,
            ballState = BallState.OUT_OF_BOUNDS,
            exitLocation = exit,
        )

        fun thrownIn(ball: Ball): Command = SetBallState(
            ball = ball,
            ballState = BallState.THROW_IN,
            carriedBy = null,
            exitLocation = null
        )
    }

    override fun execute(state: Game) {
        val ball: Ball = ball
        ball.let {
            this.originalState = it.state
            this.originalCarriedBy = it.carriedBy
            this.originalExit = it.outOfBoundsAt
            this.originalLocation = it.location
        }
        ball.let {
            it.state = ballState
            it.carriedBy = carriedBy
            if (carriedBy != null) {
                it.location = FieldCoordinate.UNKNOWN
            }
            it.outOfBoundsAt = exitLocation
            it.notifyUpdate()
            originalCarriedBy?.notifyUpdate()
        }
    }

    override fun undo(state: Game) {
        ball.state = originalState
        ball.carriedBy = originalCarriedBy
        ball.outOfBoundsAt = originalExit
        if (originalLocation != null) {
            ball.location = originalLocation!!
        }
        ball.notifyUpdate()
        ball.carriedBy?.notifyUpdate()
    }
}
