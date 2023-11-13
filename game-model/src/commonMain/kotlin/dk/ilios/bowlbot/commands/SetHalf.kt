package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game

class SetHalf(private val nextHalf: Int) : Command {

    private var originalHalf: Int = 0

    override fun execute(state: Game, controller: GameController) {
        originalHalf = state.halfNo
        state.halfNo = nextHalf
    }

    override fun undo(state: Game, controller: GameController) {
        state.halfNo = originalHalf
    }
}
