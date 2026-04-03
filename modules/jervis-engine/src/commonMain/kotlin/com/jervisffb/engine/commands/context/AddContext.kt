package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.addContext
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Save a new [ProcedureContext]
 */
class AddContext(private val context: ProcedureContext) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game) {
        state.addContext(context)
    }

    override fun undo(state: Game) {
        val value = state.contexts.remove(context::class)
        if (value != context) {
            INVALID_GAME_STATE("Attempting to remove a context that is not at the top of the stack")
        }
    }
}
