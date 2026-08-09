package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/**
 * The two possible branches of a binary event.
 */
@Serializable
enum class ChanceBranch {
    /**
     * The branch selected by the coaching when running the action path.
     * Example: Selecting a 4 on a 3+ Dodge turns it into selecting the
     * "success" branch.
     */
    SELECTED,

    /**
     * The other branch, which breaks the selected action path.
     * Example: If a 4 was selected on a 3+ Dodge, the alternative branch would
     * be the 1 and 2 values that result in a failure.
     */
    ALTERNATIVE,
}

