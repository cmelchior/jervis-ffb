package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.model.Direction
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Class representing the Random Direction Template.
 *
 * See page 20 in the BB2020 rulebook.
 */
@Serializable
object RandomDirectionTemplate {
    // The numbers are aligned to match the physical template, so
    // 1-2-3 at the top, 4-5 in the middle and 6-7-8 at the bottom.
    private val results =
        mapOf(
            1 to Direction(-1, -1),
            2 to Direction(0, -1),
            3 to Direction(1, -1),
            4 to Direction(-1, 0),
            5 to Direction(1, 0),
            6 to Direction(-1, 1),
            7 to Direction(0, 1),
            8 to Direction(1, 1),
        )

    /**
     * When the template is placed on the field (and not in a corner), roll
     * a D8 to determine the direction the object is moving in.
     */
    fun roll(roll: D8Result): Direction {
        return results[roll.value] ?: throw IllegalArgumentException("Only values between [1, 8] is allowed: ${roll.value}")
    }

    /**
     * Reverse lookup to figure out what you need to roll for a specific direction
     * @throws IllegalArgumentException if the direction does not exists
     */
    fun getRollForDirection(direction: Direction): D8Result {
        return results.entries.firstOrNull {
            it.value == direction
        }?.let {
            D8Result(it.key)
        } ?: throw IllegalArgumentException("Direction not found: $direction")
    }

    /**
     * When the template is placed in a corner, it needs to be rotated so only the
     * values 1-3 are visible. Once done, roll the D3 in order to determine the
     * direction.
     */
    fun roll(
        corner: CornerThrowInPosition,
        d3: D3Result,
    ): Direction {
        return rotateVector(results[d3.value]!!, corner.rotateDegrees)
    }

    /**
     * Returns the list of values on the template, starting from NORTH, then going clockwise
     */
    fun getTemplateValues(): List<Pair<D8Result, String>> {
        return listOf(
            2.d8 to "↑",
            3.d8 to "↗",
            5.d8 to "→",
            8.d8 to "↘",
            7.d8 to "↓",
            6.d8 to "↙",
            4.d8 to "←",
            1.d8 to "↖",
        )
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
