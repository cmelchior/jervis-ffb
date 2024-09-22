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
    // Benchmark this to see what might be best way to store these.
    // Currently using two maps to avoid allocations and ids is basically only used for Multiple Block.
    private val contexts: MutableMap<KClass<out ProcedureContext>, ProcedureContext> = mutableMapOf()
    private val contextsWithIds: MutableMap<Pair<Int, KClass<out ProcedureContext>>, ProcedureContext> = mutableMapOf()

    /**
     * Set a new context, overriding the existing one if present.
     * @return the old context, if it existed.
     */
    fun setContext(context: ProcedureContext, id: Int = 0): ProcedureContext? {
        return when(id) {
            0 -> contexts.put(context::class, context)
            else -> contextsWithIds.put(Pair(id, context::class), context)
        }
    }

    fun hasContext(contextClass: KClass<out ProcedureContext>, id: Int = 0): Boolean {
        return when(id) {
            0 -> contexts.containsKey(contextClass)
            else -> contextsWithIds.containsKey(Pair(id, contextClass))
        }
    }

    fun <T: ProcedureContext> getContext(type: KClass<T>, id: Int = 0): T? {
        val context = when(id) {
            0 -> contexts[type]
            else -> contextsWithIds[Pair(id, type)]
        }
        return if (context != null) {
            context as T
        } else {
             null
        }
    }

    fun <T: ProcedureContext> remove(type: KClass<T>, id: Int = 0): T? {
        return when(id) {
            0 -> contexts.remove(type) as T?
            else -> contextsWithIds.remove(Pair(id, type)) as T?
        }
    }
}

/**
 * Stores a new context, overriding any if they exists already.
 */
fun Game.setContext(context: ProcedureContext, id: Int = 0) {
    this.contexts.setContext(context, id)
}

/**
 * Returns the [ProcedureContext] matching the given class, or throws
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.getContext(id: Int = 0): T {
    return this.contexts.getContext(T::class, id) ?: error("Missing context ${T::class.simpleName}")
}

/**
 * Returns the [ProcedureContext] matching the given class, or throws
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.hasContext(id: Int = 0): Boolean {
    return this.contexts.getContext(T::class) != null
}
/**
 * Check if a [ProcedureContext] of a given type exists. If not
 * an [IllegalGameState] exception is thrown.
 */
inline fun <reified T: ProcedureContext> Game.assertContext() {
    if (this.contexts.getContext(T::class) == null) {
        INVALID_GAME_STATE("Missing context of type: ${T::class.qualifiedName}")
    }
}




