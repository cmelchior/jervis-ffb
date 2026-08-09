package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/** Why a physical roll exists in an actual-choice trace. */
@Serializable
enum class PhysicalRollRole {
    PRIMARY,
    ACTIVATION, // Unlocks a reroll, like Pro or Loner.
    REROLL, // Rerolls a primary roll.
}
