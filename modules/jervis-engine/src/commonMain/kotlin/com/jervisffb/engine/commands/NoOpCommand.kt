package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game

data object NoOpCommand : Command {
    override fun execute(state: Game, controller: GameController) {
        // Do nothing
    }

    override fun undo(state: Game, controller: GameController) {
        // Do nothing
    }
}
