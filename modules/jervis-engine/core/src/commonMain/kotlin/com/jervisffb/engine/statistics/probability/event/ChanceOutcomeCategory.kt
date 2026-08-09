package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable
import com.jervisffb.engine.rules.DiceRollType

/**
 * Describes what the demonstrated action path needs from a chance event.
 *
 * This is deliberately separate from [DiceRollType]. A roll type identifies the
 * rule being resolved, while this identifies how its result contributes to the
 * demonstrated path.
 */
@Serializable
enum class ChanceOutcomeCategory {
    /**
     * The demonstrated path needs a result at least as good as a minimum.
     *
     * Example: 4+ to dodge.
     */
    AT_LEAST,

    /**
     * The demonstrated path needs one of a finite set of outcomes.
     *
     * Example: Rolling Push Back on a Block die.
     * Example: Rolling a 1 on a Scatter die.
     */
    TARGET_SET,
}
