package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.TurnOver

class SetTurnOver(private val status: TurnOver?) : Command {
    private var originalValue: TurnOver? = null

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.turnOver
        state.turnOver = status
    }

    override fun undo(state: Game, controller: GameController) {
        state.turnOver = originalValue
    }
}
