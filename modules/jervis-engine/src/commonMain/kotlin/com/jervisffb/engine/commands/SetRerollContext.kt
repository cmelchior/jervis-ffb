package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.UseRerollContext

class SetRerollContext(private val value: UseRerollContext?) : Command {
    private var originalValue: UseRerollContext? = null

    override fun execute(state: Game) {
        originalValue = state.rerollContext
        state.rerollContext = value
    }

    override fun undo(state: Game) {
        state.rerollContext = originalValue
    }
}
