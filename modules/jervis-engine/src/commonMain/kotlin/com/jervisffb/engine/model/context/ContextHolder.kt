package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlin.reflect.KClass

/**
 * Container for all [ProcedureContext], which also expose a nicer API for
 * accessing them in a way that doesn't leak too many details to [Game].
 *
 * This way, any [com.jervisffb.engine.fsm.Procedure] is free to store extra
 * data in the game state without polluting the main API.
 *
 * Procedures of the same type are added in LIFO manner, making it suitable for
 * supporting nested procedures that might use the same procedure type.
 */
class ContextHolder {
    // Benchmark this to see what might be best way to store these.
    // Currently using two maps to avoid allocations, and ids are basically only used for Multiple Block.
    // Contexts are stored as stacks (MutableList) to support LIFO behavior for nested procedures.
    private val contexts: MutableMap<KClass<out ProcedureContext>, MutableList<ProcedureContext>> = mutableMapOf()
    private val contextsWithIds: MutableMap<Pair<Int, KClass<out ProcedureContext>>, MutableList<ProcedureContext>> = mutableMapOf()

    /**
     * Set a new context, pushing it onto the stack for this context type.
     * @return the old context, if it existed (the previous top of the stack).
     */
    fun setContext(context: ProcedureContext, id: Int = 0): ProcedureContext? {
        return when (id) {
            0 -> {
                val stack = contexts.getOrPut(context::class) { mutableListOf() }
                val previous = stack.lastOrNull()
                // Enable during debugging to catch duplicate contexts.
                //    if (stack.any { it::class == context::class }) {
                //        error("Context stack already contains context of type: ${context::class.simpleName}")
                //    }
                stack.add(context)
                previous
            }
            else -> {
                val key = Pair(id, context::class)
                val stack = contextsWithIds.getOrPut(key) { mutableListOf() }
                val previous = stack.lastOrNull()
                stack.add(context)
                previous
            }
        }
    }

    fun updateContext(context: ProcedureContext, id: Int = 0) {
        when (id) {
            0 -> {
                if (!contexts.containsKey(context::class) || contexts[context::class]!!.isEmpty()) {
                    error("Context stack is empty for context type: ${context::class.simpleName}")
                }
                remove(context::class, id)
                setContext(context, id)
            }
            else -> {
                val key = Pair(id, context::class)
                if (!contextsWithIds.containsKey(key) || contextsWithIds[key]!!.isEmpty()) {
                    error("Context stack is empty for context type: ${context::class.simpleName} (id: $id)")
                }
                remove(context::class, id)
                setContext(context, id)            }
        }
    }

    fun hasContext(contextClass: KClass<out ProcedureContext>, id: Int = 0): Boolean {
        return when (id) {
            0 -> contexts[contextClass]?.isNotEmpty() ?: false
            else -> contextsWithIds[Pair(id, contextClass)]?.isNotEmpty() ?: false
        }
    }

    fun <T: ProcedureContext> getContext(type: KClass<T>, id: Int = 0): T? {
        val context = when (id) {
            0 -> contexts[type]?.lastOrNull()
            else -> contextsWithIds[Pair(id, type)]?.lastOrNull()
        }
        return if (context != null) {
            @Suppress("UNCHECKED_CAST")
            context as? T ?: error("Context of type ${context::class.simpleName} is not of expected type: ${type.simpleName}")
        } else {
            null
        }
    }

    fun <T: ProcedureContext> remove(type: KClass<T>, id: Int = 0): T? {
        @Suppress("UNCHECKED_CAST")
        return when (id) {
            0 -> contexts[type]?.removeLastOrNull() as T?
            else -> contextsWithIds[Pair(id, type)]?.removeLastOrNull() as T?
        }
    }

    /**
     * Returns `true` if no contexts are stored, `false` otherwise.
     */
    fun isEmpty(): Boolean {
        val simpleContextsEmpty = contexts.all { (_, stack) -> stack.isEmpty() }
        val contextsWithIdsEmpty = contextsWithIds.all { (_, stack) -> stack.isEmpty() }
        return simpleContextsEmpty && contextsWithIdsEmpty
    }
}

/**
 * Stores a new context with a given id, overriding any if they exist already.
 */
fun Game.setContext(context: ProcedureContext, id: Int = 0) {
    this.contexts.setContext(context, id)
}

/**
 * Updates the current context with a new instance.
 */
fun Game.updateContext(context: ProcedureContext, id: Int = 0) {
    this.contexts.updateContext(context, id)
}

/**
 * Returns the last [ProcedureContext] matching the given class, or throws an error
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.getContext(id: Int = 0): T {
    return this.contexts.getContext(T::class, id) ?: error("Missing context ${T::class.simpleName}")
}

/**
 * Returns the [ProcedureContext] matching the given class, or throws
 * if none exists.
 */
inline fun <reified T: ProcedureContext> Game.getContextOrNull(id: Int = 0): T? {
    return this.contexts.getContext(T::class, id)
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
        INVALID_GAME_STATE("Missing context of type: ${T::class.simpleName}")
    }
}




