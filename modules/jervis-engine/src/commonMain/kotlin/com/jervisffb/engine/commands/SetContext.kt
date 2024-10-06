package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.setContext

/**
 * Save a new [ProcedureContext]
 */
class SetContext(private val context: ProcedureContext) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.contexts.getContext(context::class)
        state.setContext(context)
    }

    override fun undo(state: Game, controller: GameController) {
        if (originalValue == null) {
            state.contexts.remove(context::class)
        } else {
            state.setContext(originalValue!!)
        }
    }
}
