package com.jervisffb.engine.rules.common.roster

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class RosterId(val id: String)

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

interface BB20216Roster : Roster
