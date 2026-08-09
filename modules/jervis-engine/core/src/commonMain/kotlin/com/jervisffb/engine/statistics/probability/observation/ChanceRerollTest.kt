package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.rules.DiceRollType
import kotlinx.serialization.Serializable

/** A test which can occur before or after a reroll source is used. */
@Serializable
data class ChanceRerollTest(
    val rollType: DiceRollType,
    val dieSides: Int,
    val successTarget: Int,
    val effect: ChanceRerollTestEffect,
)
