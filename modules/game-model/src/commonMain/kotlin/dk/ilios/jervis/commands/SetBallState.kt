package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetBallState private constructor(
    private val ballState: BallState,
    private val carriedBy: Player? = null,
    private val exitLocation: FieldCoordinate? = null,
) : Command {
    private lateinit var originalState: BallState
    private var originalCarriedBy: Player? = null
    private var originalExit: FieldCoordinate? = null
    private var originalLocation: FieldCoordinate? = null

    companion object {
        fun accurateThrow(): Command = SetBallState(ballState = BallState.ACCURATE_THROW, carriedBy = null, exitLocation = null)

        fun inAir(): Command = SetBallState(ballState = BallState.IN_AIR, carriedBy = null, exitLocation = null)

        fun carried(player: Player): Command = SetBallState(ballState = BallState.CARRIED, carriedBy = player, exitLocation = null)

        fun onGround(): Command = SetBallState(ballState = BallState.ON_GROUND, carriedBy = null, exitLocation = null)

        fun deviating(): Command = SetBallState(ballState = BallState.DEVIATING, carriedBy = null, exitLocation = null)

        fun bouncing(): Command = SetBallState(ballState = BallState.BOUNCING, carriedBy = null, exitLocation = null)

        fun scattered(): Command = SetBallState(ballState = BallState.SCATTERED, carriedBy = null, exitLocation = null)

        fun outOfBounds(exit: FieldCoordinate): Command =
            SetBallState(
                ballState = BallState.OUT_OF_BOUNDS,
                exitLocation = exit,
            )

        fun thrownIn(): Command = SetBallState(BallState.THROW_IN, carriedBy = null, exitLocation = null)
    }

    override fun execute(state: Game, controller: GameController) {
        val ball: Ball = state.ball
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

    override fun undo(state: Game, controller: GameController) {
        state.ball.state = originalState
        state.ball.carriedBy = originalCarriedBy
        state.ball.outOfBoundsAt = originalExit
        if (originalLocation != null) {
            state.ball.location = originalLocation!!
        }
        state.ball.notifyUpdate()
        state.ball.carriedBy?.notifyUpdate()
    }
}
