package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

class SetTurnOver(private val status: Boolean): Command {
    var originalState: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalState = state.isTurnOver
        state.isTurnOver = status
    }

    override fun undo(state: Game, controller: GameController) {
        state.isTurnOver = originalState
    }
}
