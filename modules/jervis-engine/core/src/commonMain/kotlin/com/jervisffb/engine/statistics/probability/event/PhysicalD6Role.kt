package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/** Why a physical D6 exists in an actual-choice trace. */
@Serializable
enum class PhysicalD6Role {
    PRIMARY,
    ACTIVATION,
    REROLL,
}

