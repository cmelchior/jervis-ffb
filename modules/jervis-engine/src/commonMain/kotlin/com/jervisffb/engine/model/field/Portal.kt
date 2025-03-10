package com.jervisffb.engine.model.field

import kotlinx.serialization.Serializable

/**
 * This class represents a Portal as described on page 32 in the Dungeon Bowl rulebook.
 */
@Serializable
data class Portal(val number: Int)
