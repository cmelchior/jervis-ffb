package com.jervisffb.engine.rules.common.roster

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
    operator fun get(id: PositionId): Position = positions.first { it.id == id }
}
