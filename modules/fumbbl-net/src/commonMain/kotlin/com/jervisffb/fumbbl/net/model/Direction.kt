package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.api.serialization.FumbblEnum
import com.jervisffb.fumbbl.net.api.serialization.FumbblEnumSerializer
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
    fun transformToJervisDirection(): com.jervisffb.engine.model.Direction {
        return when (this) {
            NORTH -> com.jervisffb.engine.model.Direction(0, -1)
            NORTHEAST -> com.jervisffb.engine.model.Direction(1, -1)
            EAST -> com.jervisffb.engine.model.Direction(1, 0)
            SOUTHEAST -> com.jervisffb.engine.model.Direction(1, 1)
            SOUTH -> com.jervisffb.engine.model.Direction(0, 1)
            SOUTHWEST -> com.jervisffb.engine.model.Direction(-1, 1)
            WEST -> com.jervisffb.engine.model.Direction(-1, 0)
            NORTHWEST -> com.jervisffb.engine.model.Direction(-1, -1)
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
