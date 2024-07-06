package dk.ilios.jervis.procedures

import dk.ilios.jervis.actions.BlockDice
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource

sealed interface DieRoll

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
data class BlockDieRoll(
    val originalRoll: DBlockResult,
    var rerollSource: RerollSource? = null,
    var rerolledResult: DBlockResult? = null
): DieRoll {
    val result: BlockDice
        get() = rerolledResult?.blockResult ?: originalRoll.blockResult
}

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
data class D6DieRoll(
    val originalRoll: D6Result,
    var rerollSource: RerollSource? = null,
    var rerolledResult: D6Result? = null
): DieRoll {
    val result: D6Result
        get() = rerolledResult ?: originalRoll
}

data class RerollContext(val roll: DiceRollType, val source: RerollSource)

data class RerollResultContext(val roll: DiceRollType, val source: RerollSource, val rerollAllowed: Boolean)