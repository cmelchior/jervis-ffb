package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate
import kotlinx.serialization.Serializable

/**
 * A vector describing a direction on a Blood Bowl field. Usually used as
 * the result of a roll using the Random Direction Template.
 *
 * The [xModifier] and [yModifier] are the delta that needs to be applied to a
 * [FieldCoordinate] in order to move it 1 square in the desired direction.
 */
@Serializable
data class Direction(val xModifier: Int, val yModifier: Int) {

    /**
     * Returns the reverse direction
     */
    fun reverse(): Direction = Direction(xModifier * -1, yModifier * -1)

    /**
     * Named directions. These represent a direction as defined on the internal
     * field model and might not present the direction shown in the UI.
     */
    companion object {
        val UP_LEFT = Direction(0, -1)
        val UP = Direction(0, -1)
        val UP_RIGHT = Direction(0, -1)
        val LEFT = Direction(-1, 0)
        val RIGHT = Direction(1, 0)
        val BOTTOM_LEFT = Direction(-1, 1)
        val BOTTOM = Direction(0, 1)
        val BOTTOM_RIGHT = Direction(1, 1)

        /**
         * Returns the direction from [origin] towards [destination].
         */
        fun from(origin: FieldCoordinate, destination: FieldCoordinate): Direction {
            return Direction(destination.x - origin.x, destination.y - origin.y)
        }
    }
}
