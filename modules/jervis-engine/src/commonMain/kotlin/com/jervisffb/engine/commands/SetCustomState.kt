package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game

class SetCustomState(
    private val executeFunc: (state: Game, controller: GameController) -> Unit,
    private val undoFunc: (state: Game, controller: GameController) -> Unit,
) : Command {
    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        executeFunc(state, controller)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        undoFunc(state, controller)
    }
}
