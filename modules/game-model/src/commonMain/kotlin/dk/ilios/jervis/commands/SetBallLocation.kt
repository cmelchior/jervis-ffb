package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Ball
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.assert

class SetBallLocation(val ball: Ball, val location: FieldCoordinate) : Command {
    private lateinit var originalLocation: FieldCoordinate

    override fun execute(state: Game, controller: GameController) {
        assert(ball.state != BallState.CARRIED)
        val rules: Rules = controller.rules
        this.originalLocation = ball.location
        ball.location = location
        if (originalLocation.isOnField(rules)) {
            state.field[originalLocation].apply {
                balls.remove(ball)
                notifyUpdate()
            }
        }
        if (location.isOnField(rules)) {
            state.field[location].apply {
                balls.add(ball)
                notifyUpdate()
            }
        }
    }

    override fun undo(state: Game, controller: GameController) {
        val rules = controller.rules
        if (location.isOnField(rules)) {
            state.field[location].apply {
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
