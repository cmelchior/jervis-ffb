package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

class SetHalf(private val nextHalf: Int) : Command {
    private var originalHalf: Int = 0

    override fun execute(state: Game) {
        originalHalf = state.halfNo
        state.halfNo = nextHalf
    }

    override fun undo(state: Game) {
        state.halfNo = originalHalf
    }
}
