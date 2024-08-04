package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer
import kotlinx.serialization.Serializable

class DirectionSerializer : FumbblEnumSerializer<Direction>(Direction::class)

@Serializable(with = DirectionSerializer::class)
enum class Direction(override val id: String) : FumbblEnum {
    NORTH("North"),
    NORTHEAST("Northeast"),
    EAST("East"),
    SOUTHEAST("Southeast"),
    SOUTH("South"),
    SOUTHWEST("Southwest"),
    WEST("West"),
    NORTHWEST("Northwest"),
    ;

    /**
     * Transform a Direction in FUMBBL to a Direection in Jervis.
     */
    fun transformToJervisDirection(): dk.ilios.jervis.rules.tables.Direction {
        return when (this) {
            NORTH -> dk.ilios.jervis.rules.tables.Direction(0, -1)
            NORTHEAST -> dk.ilios.jervis.rules.tables.Direction(1, -1)
            EAST -> dk.ilios.jervis.rules.tables.Direction(1, 0)
            SOUTHEAST -> dk.ilios.jervis.rules.tables.Direction(1, 1)
            SOUTH -> dk.ilios.jervis.rules.tables.Direction(0, 1)
            SOUTHWEST -> dk.ilios.jervis.rules.tables.Direction(-1, 1)
            WEST -> dk.ilios.jervis.rules.tables.Direction(-1, 0)
            NORTHWEST -> dk.ilios.jervis.rules.tables.Direction(-1, -1)
        }
    }

    fun reverse(): Direction {
        return when (this) {
            NORTH -> SOUTH
            NORTHEAST -> SOUTHWEST
            EAST -> WEST
            SOUTHEAST -> NORTHWEST
            SOUTH -> NORTH
            SOUTHWEST -> NORTHEAST
            WEST -> EAST
            NORTHWEST -> SOUTHEAST
        }
    }

    /**
     * Swap around the x-axis
     */
    fun swap(): Direction {
        return when (this) {
            NORTHEAST -> NORTHWEST
            EAST -> WEST
            SOUTHEAST -> SOUTHWEST
            SOUTHWEST -> SOUTHEAST
            WEST -> EAST
            NORTHWEST -> NORTHEAST
            else -> this
        }
    }

    companion object {
        fun forName(name: String?): Direction? {
            return values().firstOrNull { it.id == name }
        }
    }
}
