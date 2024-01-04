package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnum
import dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer

class DirectionSerializer: FumbblEnumSerializer<Direction>(Direction::class)

@Serializable(with = DirectionSerializer::class)
enum class Direction(override val id: String): FumbblEnum {
    NORTH("North"),
    NORTHEAST("Northeast"),
    EAST("East"),
    SOUTHEAST("Southeast"),
    SOUTH("South"),
    SOUTHWEST("Southwest"),
    WEST("West"),
    NORTHWEST("Northwest");

    fun transform(): Direction {
        return when (this) {
            Direction.NORTHEAST -> Direction.NORTHWEST
            Direction.EAST -> Direction.WEST
            Direction.SOUTHEAST -> Direction.SOUTHWEST
            Direction.SOUTHWEST -> Direction.SOUTHEAST
            Direction.WEST -> Direction.EAST
            Direction.NORTHWEST -> Direction.NORTHEAST
            else -> this
        }
    }

    companion object {
        fun forName(name: String?): Direction? {
            return values().firstOrNull { it.id == name }
        }
    }
}