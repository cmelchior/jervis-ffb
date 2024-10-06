package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.setContext
import kotlin.reflect.KClass

/**
 * Helper method for making it slightly cleaner to represent this command
 */
inline fun <reified T: ProcedureContext> RemoveContext(): Command {
    return RemoveContext(T::class)
}

/**
 * Remove a [ProcedureContext] of a given type.
 */
class RemoveContext<T: ProcedureContext>(private val type: KClass<T>) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.contexts.getContext(type)
        state.contexts.remove(type)
    }

    override fun undo(state: Game, controller: GameController) {
//        if (originalValue == null) {
            state.setContext(originalValue!!)
//        }
    }
}
