package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.setContext
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

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = state.contexts.getContext(type)
        state.contexts.remove(type)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        if (originalValue == null) {
            state.setContext(originalValue!!)
        }
    }
}
