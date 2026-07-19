package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

data object NoOpCommand : Command {
    override fun execute(state: Game) {
        // Do nothing
    }

    override fun undo(state: Game) {
        // Do nothing
    }
}
