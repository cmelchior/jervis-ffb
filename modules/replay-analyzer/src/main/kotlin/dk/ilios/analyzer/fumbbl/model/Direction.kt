package dk.ilios.analyzer.fumbbl.model

import dk.ilios.analyzer.fumbbl.net.serialization.FumbblEnumSerializer
import dk.ilios.analyzer.fumbbl.net.serialization.FumbblEnum
import kotlinx.serialization.Serializable

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