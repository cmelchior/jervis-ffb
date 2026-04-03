package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

class SetAbortIfBallOutOfBounds(private val abort: Boolean) : Command {
    private var originalValue: Boolean = false
    override fun execute(state: Game) {
        originalValue = state.abortIfBallOutOfBounds
        state.abortIfBallOutOfBounds = abort
    }

    override fun undo(state: Game) {
        state.abortIfBallOutOfBounds = originalValue
    }
}
