package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert

class SetBallLocation(val location: FieldCoordinate) : Command {
    private lateinit var originalLocation: FieldCoordinate

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        assert(state.ball.state != BallState.CARRIED)
        val rules: Rules = controller.rules
        this.originalLocation = state.ball.location
        state.ball.location = location
        if (originalLocation.isOnField(rules)) {
            state.field[originalLocation].apply {
                ball = null
                notifyUpdate()
            }
        }
        if (location.isOnField(rules)) {
            state.field[location].apply {
                ball = state.ball
                notifyUpdate()
            }
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        val rules = controller.rules
        if (location.isOnField(rules)) {
            state.field[location].apply {
                ball = null
                notifyUpdate()
            }
        }
        if (originalLocation.isOnField(rules)) {
            state.field[originalLocation].apply {
                ball = state.ball
                notifyUpdate()
            }
        }
        state.ball.location = originalLocation
    }
}
