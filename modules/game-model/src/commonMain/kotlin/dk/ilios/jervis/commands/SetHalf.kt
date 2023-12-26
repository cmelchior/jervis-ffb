package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

class SetHalf(private val nextHalf: UInt) : Command {

    private var originalHalf: UInt = 0u

    override fun execute(state: Game, controller: GameController) {
        originalHalf = state.halfNo
        state.halfNo = nextHalf
    }

    override fun undo(state: Game, controller: GameController) {
        state.halfNo = originalHalf
    }
}
