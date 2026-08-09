package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.statistics.probability.Probability

val ActionPathEvent.owner: TeamId?
    get() = when (this) {
        is ActionPathEvent.Block -> owner
        is ActionPathEvent.D6 -> owner
        is ActionPathEvent.PhysicalD6 -> owner
        is ActionPathEvent.Unsupported -> null
    }

internal val ActionPathEvent.scope: ActionPathEventScope?
    get() = when (this) {
        is ActionPathEvent.Block -> scope
        is ActionPathEvent.D6 -> scope
        is ActionPathEvent.PhysicalD6 -> scope
        is ActionPathEvent.Unsupported -> null
    }

val ActionPathEvent.recoveries: List<RerollOption>
    get() = when (this) {
        is ActionPathEvent.Block -> recoveries
        is ActionPathEvent.D6 -> recoveries
        is ActionPathEvent.PhysicalD6 -> recoveries
        is ActionPathEvent.Unsupported -> emptyList()
    }

val ActionPathEvent.observedOutcomeProbability: Probability
    get() = when (this) {
        is ActionPathEvent.Block -> observedOutcomeProbability
        is ActionPathEvent.D6 -> observedOutcome.probability
        is ActionPathEvent.PhysicalD6 -> observedOutcome.probability
        is ActionPathEvent.Unsupported -> error("Unsupported observations have no probability")
    }

