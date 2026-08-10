package com.jervisffb.engine.statistics.probability.observation

import kotlinx.serialization.Serializable

/** Identifies one result within one physical dice roll. */
@Serializable
data class ChanceResultId(
    // Quick reference to `ChanceObservation.DiceRoll.index`
    val rollIndex: Int,
    // Index of the die within the dice pool.
    val dicePoolIndex: Int,
)
