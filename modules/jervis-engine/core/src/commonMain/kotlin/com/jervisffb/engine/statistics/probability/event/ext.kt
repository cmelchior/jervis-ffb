package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.statistics.probability.Probability

val ActionPathEvent.owner: TeamId?
    get() = when (this) {
        is ActionPathEvent.Logical -> owner
        is ActionPathEvent.Physical -> owner
        is ActionPathEvent.Unsupported -> null
    }

internal val ActionPathEvent.scope: ActionPathEventScope?
    get() = when (this) {
        is ActionPathEvent.Logical -> scope
        is ActionPathEvent.Physical -> scope
        is ActionPathEvent.Unsupported -> null
    }

val ActionPathEvent.recoveries: List<RerollOption>
    get() = when (this) {
        is ActionPathEvent.Logical -> recoveries
        is ActionPathEvent.Physical -> recoveries
        is ActionPathEvent.Unsupported -> emptyList()
    }

val ActionPathEvent.observedOutcomeProbability: Probability
    get() = when (this) {
        is ActionPathEvent.Logical -> observedOutcome.probability
        is ActionPathEvent.Physical -> observedOutcome.probability
        is ActionPathEvent.Unsupported -> error("Unsupported observations have no probability")
    }
