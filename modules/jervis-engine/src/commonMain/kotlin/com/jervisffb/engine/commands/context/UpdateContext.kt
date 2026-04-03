package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.addContext
import com.jervisffb.engine.model.context.updateContext
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Update the last [ProcedureContext] of the current stack.
 * If the stack for this type is empty, an error is thrown.
 */
class UpdateContext(private val context: ProcedureContext) : Command {
    lateinit var originalValue: ProcedureContext

    override fun execute(state: Game) {
        originalValue = state.contexts.getContext(context::class) ?: INVALID_GAME_STATE("Attempting to update a context that doesn't exists.")
        state.updateContext(context)
    }

    override fun undo(state: Game) {
        val currentContext = state.contexts.remove(context::class)
        if (currentContext != context) {
            INVALID_GAME_STATE("Wrong context at the top of the stack.")
        }
        state.addContext(originalValue)
    }
}
