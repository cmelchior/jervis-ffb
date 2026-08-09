package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/** How often a [RerollResource] can be consumed. */
@Serializable
enum class RerollUsage {
    REUSABLE, // The source is available independently for every event.
    ONCE_PER_ACTION,
    ONCE_PER_ACTIVATION,
    ONCE_PER_TURN,
    ONCE_PER_DRIVE,
    ONCE_PER_HALF,
    ONCE_PER_GAME,
    UNSUPPORTED, // The source cannot be represented safely by the current state model.
}
