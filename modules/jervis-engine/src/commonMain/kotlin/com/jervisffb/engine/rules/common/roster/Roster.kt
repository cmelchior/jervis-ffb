package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.serialize.SpriteSource

interface Race {
    val id: Long
    val name: String
}

interface Roster {
    val id: RosterId
    val name: String
    val numberOfRerolls: Int
    val rerollCost: Int
    val allowApothecary: Boolean
    val positions: List<Position>
    val rosterLogo: SpriteSource?
    operator fun get(id: PositionId): Position = positions.first { it.id == id }
}
