package dk.ilios.jervis.model.context

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlin.reflect.KClass

/**
 * Container for all [ProcedureContext], which also expose a nicer API for
 * accessing them in a way that doesn't leak too many details to [Game].
 *
 * This way, any [Procedure] is free to store extra data in the game state without
 * polluting the main state.
 */
class ContextHolder {
    private val contexts: MutableMap<KClass<out ProcedureContext>, ProcedureContext> = mutableMapOf()

    /**
     * Set a new context, overriding the existing one if present.
     * @return the old context, if it existed.
     */
    fun setContext(context: ProcedureContext): ProcedureContext? {
        return contexts.put(context::class, context)
    }

    fun hasContext(contextClass: KClass<out ProcedureContext>): Boolean {
        return contexts.containsKey(contextClass)
    }

    fun <T: ProcedureContext> getContext(type: KClass<T>): T? {
        val context = contexts[type]
        return if (context != null) {
            context as T
        } else {
             null
        }
    }

    fun <T: ProcedureContext> remove(type: KClass<T>): T? {
        return contexts.remove(type) as T?
    }
}

/**
 * Stores a new context, overriding any if they exists already.
 */
fun Game.setContext(context: ProcedureContext) {
    this.contexts.setContext(context)
}

/**
 * Returns the [ProcedureContext] matching the given class, or throws
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.getContext(): T {
    return this.contexts.getContext(T::class)!!
}

/**
 * Returns the [ProcedureContext] matching the given class, or throws
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.hasContext(): Boolean {
    return this.contexts.getContext(T::class) != null
}
/**
 * Check if a [ProcedureContext] of a given type exists. If not
 * an [IllegalGameState] exception is thrown.
 */
inline fun <reified T: ProcedureContext> Game.assertContext() {
    if (this.contexts.getContext(T::class) == null) {
        INVALID_GAME_STATE("Missing context of type: ${T::class}")
    }
}




