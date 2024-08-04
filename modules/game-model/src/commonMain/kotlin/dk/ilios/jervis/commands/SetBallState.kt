package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
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
        fun inAir(): Command = SetBallState(ballState = BallState.IN_AIR)

        fun carried(player: Player): Command = SetBallState(ballState = BallState.CARRIED, carriedBy = player)

        fun onGround(): Command = SetBallState(ballState = BallState.ON_GROUND)

        fun deviating(): Command = SetBallState(ballState = BallState.DEVIATING)

        fun bouncing(): Command = SetBallState(ballState = BallState.BOUNCING)

        fun outOfBounds(exit: FieldCoordinate): Command =
            SetBallState(
                ballState = BallState.OUT_OF_BOUNDS,
                exitLocation = exit,
            )
    }

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
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
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.ball.state = originalState
        state.ball.carriedBy = originalCarriedBy
        state.ball.outOfBoundsAt = originalExit
        if (originalLocation != null) {
            state.ball.location = originalLocation!!
        }
        state.ball.notifyUpdate()
    }
}
