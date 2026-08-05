package com.jervisffb.engine.challenge

/**
 * Helper class for building challenge goals.
 */
abstract class GoalBuilder<T: ChallengeGoal, B: GoalBuilder<T, B>> {
    protected val modifiers = mutableListOf<GoalModifier>()

    fun addModifier(modifier: GoalModifier): B {
        modifiers.add(modifier)
        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    fun addModifiers(vararg modifiers: GoalModifier): B {
        this.modifiers.addAll(modifiers)
        @Suppress("UNCHECKED_CAST")
        return this as B
    }

    abstract fun build(): T
}
