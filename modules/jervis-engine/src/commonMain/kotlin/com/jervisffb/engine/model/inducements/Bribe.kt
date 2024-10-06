package com.jervisffb.engine.model.inducements

import kotlinx.serialization.Serializable

/**
 * Class describing a Bribe that has been assigned to a team.
 * Its purpose is to track the usage of the Bribe during a game,
 * and now how/when to purchase it.
 *
 * See page 91 in the rulebook.
 */
@Serializable
data class Bribe(var used: Boolean = false)
