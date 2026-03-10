package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.setContext
import com.jervisffb.engine.model.context.updateContext

/**
 * Update the last [ProcedureContext] of the current stack.
 * If the stack for this type is empty, an error is thrown.
 */
class UpdateContext(private val context: ProcedureContext) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game) {
        originalValue = state.contexts.getContext(context::class)
        state.updateContext(context)
    }

    override fun undo(state: Game) {
        if (originalValue == null) {
            state.contexts.remove(context::class)
        } else {
            state.setContext(originalValue!!)
        }
    }
}
