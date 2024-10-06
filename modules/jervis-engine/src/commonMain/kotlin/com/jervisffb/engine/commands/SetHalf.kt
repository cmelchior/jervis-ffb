package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game

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
