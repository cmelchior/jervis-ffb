package dk.ilios.jervis.procedures

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource

sealed interface DieRoll<D: DieResult, R: DieResult> {
    val originalRoll: D
    var rerollSource: RerollSource?
    var rerolledResult: D?
    val result: R
}

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
data class BlockDieRoll(
    override val originalRoll: DBlockResult,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: DBlockResult? = null,
): DieRoll<DBlockResult, DBlockResult> {
    override val result: DBlockResult
        get() = rerolledResult ?: originalRoll
}

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
data class D6DieRoll(
    override val originalRoll: D6Result,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: D6Result? = null,
): DieRoll<D6Result, D6Result> {
    override val result: D6Result
        get() = rerolledResult ?: originalRoll
}

data class RerollContext(val roll: DiceRollType, val source: RerollSource)

// Wraps the result of whether a specific reroll source can be used to reroll a specific roll type (all dice or parts of them)
data class RerollResultContext(val roll: DiceRollType, val source: RerollSource, val rerollAllowed: Boolean)