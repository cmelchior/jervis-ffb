package com.jervisffb.engine.rng

import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult

/**
 * Interface for rolling dice.
 *
 * Implementations of this interface are free to choose how they do it. So the returned
 * results can either be random or following some fixed pattern.
 */
interface DiceRollGenerator {
    fun rollDie(die: Dice): DieResult
}
