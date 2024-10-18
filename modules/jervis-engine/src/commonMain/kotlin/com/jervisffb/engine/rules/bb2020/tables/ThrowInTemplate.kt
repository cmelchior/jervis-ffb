package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Direction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


enum class ThrowInPosition private constructor(val rotateDegrees: Int) {
    TOP(180),
    BOTTOM(0),
    LEFT(90),
    RIGHT(-90),
}
/**
 * Class representing the Throw-in Template.
 * See page 51 in the rulebook
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
