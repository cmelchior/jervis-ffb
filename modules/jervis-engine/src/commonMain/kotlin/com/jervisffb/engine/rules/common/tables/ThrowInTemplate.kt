package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Direction
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Class representing the Throw-in Template.
 *
 * See page 51 in the BB2020 rulebook.
 */
object ThrowInTemplate {
    // Order of numbers are from left to right, when the template is placed at the bottom
    private val results =
        mapOf(
            1 to Direction(-1, -1),
            2 to Direction(0, -1),
            3 to Direction(1, -1),
        )

    /**
     * When the template is placed in a corner, it needs to be rotated so only the
     * values 1-3 are visible. Once done, roll the D3 to determine the direction.
     */
    fun roll(
        position: ThrowInPosition,
        d3: D3Result,
    ): Direction {
        return rotateVector(results[d3.value]!!, position.rotateDegrees)
    }

    /**
     * When the template is placed anywhere on the pitch and pointed in a specific direction,
     * compute the rotation angle from that direction and apply the throw-in template.
     *
     * @param direction The direction determining the "center" of the throw-in template.
     * @return the result of rolling on the template, the result will either be [direction] or one of the squares next to it
     */
    fun roll(direction: Direction, d3: D3Result): Direction {
        // The base template (0°) points UP (0,-1); the angle is computed relative to that.
        val angleRadians = atan2(direction.yModifier.toDouble(), direction.xModifier.toDouble()) + PI / 2
        val angleDegrees = (angleRadians * 180.0 / PI).roundToInt()
        return rotateVector(results[d3.value]!!, angleDegrees)
    }

    private fun rotateVector(
        vector: Direction,
        angleDegrees: Int,
    ): Direction {
        // Use the Rotation Matrix to rotate the coordinates
        val angleRadians = angleDegrees * PI / 180.0
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        val x: Double = vector.xModifier * cosTheta - vector.yModifier * sinTheta
        val y = vector.xModifier * sinTheta + vector.yModifier * cosTheta
        return Direction(x.roundToInt(), y.roundToInt())
    }
}
