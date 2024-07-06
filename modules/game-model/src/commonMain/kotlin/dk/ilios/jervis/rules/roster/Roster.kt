package dk.ilios.jervis.rules.roster

import kotlin.jvm.JvmInline

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
}

interface BB20216Roster: Roster {

}

