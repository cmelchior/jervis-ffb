package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

class SetHalf(private val nextHalf: Int) : Command {
    private var originalHalf: Int = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalHalf = state.halfNo
        state.halfNo = nextHalf
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.halfNo = originalHalf
    }
}
