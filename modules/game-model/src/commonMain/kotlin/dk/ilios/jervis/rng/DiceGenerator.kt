package dk.ilios.jervis.rng

import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DieResult

/**
 * Interface for rolling dice.
 *
 * Implementations of this interface are free to choose how they do it. So the returned
 * results can either be random or following some fixed pattern.
 */
interface DiceGenerator {
    fun rollDie(die: Dice): DieResult
}
