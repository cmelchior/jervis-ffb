package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.RerollSourceId
import kotlinx.serialization.Serializable

/**
 * Interface allowing us to track a single die all the way from rolling it
 * the first time to it getting rerolled. Dice should never be tracked alone
 * but always as part of a [com.jervisffb.engine.actions.DicePool].
 */
@Serializable
sealed interface DieRoll<D : DieResult> {
    // How unique does this need to be?
    val id: DieId
    // The value of the first roll of the die
    val originalRoll: D
    // What source allowed the die to be rerolled (if it was rerolled)
    var rerollSource: RerollSourceId?
    // The value of the die after it being rerolled.
    var rerolledResult: D?
    // Returns the current value of the die (just makes it easier to get the
    // value without having to worry about rerolls)
    val result: D
}
