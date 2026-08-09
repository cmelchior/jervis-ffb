package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/** What happens when a recovery's activation roll fails. */
@Serializable
enum class ActivationFailureBehavior {
    /** Keep the original result and do not try another recovery. */
    STOP,

    /** Reserved for mechanics that v1 deliberately refuses to approximate. */
    UNSUPPORTED,
}

