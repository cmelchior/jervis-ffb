package com.jervisffb.engine.challenge

import kotlin.reflect.KClass

/**
 * This class holds all contexts for a given [ChallengeStep]. It should not
 * be used across steps.
 */
class ChallengeContextHolder(
    private val contexts: Map<KClass<out ChallengeContext>, ChallengeContext>,
) {
    constructor(contexts: List<ChallengeContext?>): this(contexts.filterNotNull().associateBy { it::class })

    fun <T: ChallengeContext> get(type: KClass<T>): ChallengeContext {
        return contexts[type] ?: error("Missing context ${type.simpleName}")
    }

    inline fun <reified T: ChallengeContext> get(): T {
        return this.get(T::class) as T
    }
}
