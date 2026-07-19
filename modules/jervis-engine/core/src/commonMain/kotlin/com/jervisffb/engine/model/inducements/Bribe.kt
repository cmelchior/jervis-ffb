package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Class describing a Bribe that has been assigned to a team.
 * Its purpose is to track the usage of the Bribe during a game,
 * and not how/when to purchase it.
 *
 * See page 91 in the BB202 rulebook.
 * See page 144 in the BB2025 rulebook.
 */
@Serializable
data class Bribe(var used: Boolean = false, val duration: Duration = Duration.END_OF_GAME)
