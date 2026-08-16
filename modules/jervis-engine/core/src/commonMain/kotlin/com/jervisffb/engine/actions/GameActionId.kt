package com.jervisffb.engine.actions

import kotlinx.serialization.Serializable

/**
 * This ID uniquely identifies a [GameAction] that has been handled by the
 * [GameEngineController]. The [counter] increases for every handled action,
 * while [generation] increases whenever a [Revert] starts a new timeline.
 *
 * This also makes it possible to reason about multiple events arriving
 * at the GameController. If it sees a GameAction with an ID that has already
 * been processed, the next action with the same ID should be ignored (or throw
 * an error).
 *
 * Any value of either [counter] or [generation] less than 1 is an error.
 */
@Serializable
data class GameActionId(
    val counter: Int,
    val generation: Int = START_GENERATION,
) {
    operator fun plus(increment: Int): GameActionId {
        return copy(counter = counter + increment)
    }

    operator fun minus(increment: Int): GameActionId {
        return copy(counter = counter - increment)
    }

    operator fun compareTo(other: GameActionId): Int {
        return when (val generationComparison = generation.compareTo(other.generation)) {
            0 -> counter.compareTo(other.counter)
            else -> generationComparison
        }
    }

    /**
     * Branch off a new timeline at the current id.
     * This returns a new [GameActionId] with the same [counter] but a new
     * [generation].
     */
    fun branch(): GameActionId = copy(generation = generation + 1)

    /**
     * Returns the expected next game id.
     */
    fun next(): GameActionId {
        return this + 1
    }

    override fun toString(): String {
        return "GameActionId[$counter,$generation]"
    }

    companion object {
        const val START_COUNTER = 1
        const val START_GENERATION = 1
        val INITIAL = GameActionId(0, 0)
        val NONE = GameActionId(-1, 1)
    }
}
