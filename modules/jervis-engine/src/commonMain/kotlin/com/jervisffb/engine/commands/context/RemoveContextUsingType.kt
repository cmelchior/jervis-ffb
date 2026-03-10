package com.jervisffb.engine.commands.context

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.setContext
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlin.reflect.KClass

// We need to introduce two helper function "constructors" here to make the API
// a bit nicer.

/**
 * Helper method for making it slightly cleaner to represent this command
 */
@Suppress("FunctionName")
inline fun <reified T: ProcedureContext> RemoveContext(): Command {
    return RemoveContextUsingType(T::class)
}

/**
 * Helper method for removing an instance of a [ProcedureContext]. It will throw
 * an exception if it is not at the top of the stack for its type.
 */
@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun RemoveContext(context: ProcedureContext): Command =
    RemoveContextUsingReference(context)

class RemoveContextUsingType<T: ProcedureContext>(private val type: KClass<T>) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game) {
        originalValue = state.contexts.getContext(type)
        if (state.contexts.remove(type) == null) {
            INVALID_GAME_STATE("Attempting to remove a context that could not be found: $type")
        }
    }

    override fun undo(state: Game) {
        if (originalValue != null) {
            state.setContext(originalValue!!)
        }
    }
}

class RemoveContextUsingReference(private val context: ProcedureContext) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game) {
        originalValue = state.contexts.remove(context::class)
        if (originalValue != context) {
            throw IllegalStateException("Attempting to remove a context that is not at the top of the stack.")
        }
    }

    override fun undo(state: Game) {
        if (originalValue != null) {
            state.setContext(originalValue!!)
        }
    }
}


