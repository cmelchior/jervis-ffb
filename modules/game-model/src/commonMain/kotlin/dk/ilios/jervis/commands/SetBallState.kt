package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player

class SetBallState(private val ballState: BallState) : Command {
    private lateinit var originalState: BallState
    override fun execute(state: Game, controller: GameController) {
        this.originalState = state.ball.state
        state.ball.state = ballState
    }

    override fun undo(state: Game, controller: GameController) {
        state.ball.state = originalState
    }
}
