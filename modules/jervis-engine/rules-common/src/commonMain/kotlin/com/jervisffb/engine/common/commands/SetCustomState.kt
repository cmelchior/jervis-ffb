package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game

class SetCustomState(
    private val executeFunc: (state: Game) -> Unit,
    private val undoFunc: (state: Game) -> Unit,
) : Command {
    override fun execute(
        state: Game,
    ) {
        executeFunc(state)
    }

    override fun undo(
        state: Game,
    ) {
        undoFunc(state)
    }
}
