package com.jervisffb.engine.rules.builder

import com.jervisffb.engine.model.StadiumType
import kotlinx.serialization.Serializable

/**
 * Interface representing which stadium is used for this game.
 *
 * [NoStadium] is used for a normal game of Blood Bowl.
 * [RollForStadiumUsed] is used if you want to use the rules for "Blood Bowl Stadia"
 * as described in Death Zone page 60.
 * [SpecificStadium] is an alternative option for just choosing whatever stadium is fun
 * to use.
 */
@Serializable
sealed interface StadiumRule

@Serializable
data object NoStadium: StadiumRule
@Serializable
data object RollForStadiumUsed: StadiumRule
@Serializable
data class SpecificStadium(val type: StadiumType): StadiumRule
