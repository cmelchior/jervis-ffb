package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.model.FieldCoordinate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 *  Determines in what corner the Random Direction Template is placed.
 *  "Top" is defined as the direction towards 0 on the x-axis for the [FieldCoordinate]
 *  "Left" is defined as the direction towards 0 on the y-axis for the [FieldCoordinate]
 */
enum class CornerThrowInPosition(val rotateDegrees: Int) {
    TOP_LEFT(135),
    TOP_RIGHT(-135),
    BOTTOM_RIGHT(-45),
    BOTTOM_LEFT(45)
}

/**
 * Class representing the Random Direction Template.
 * See page 20 in the rulebook
 */
object RandomDirectionTemplate {

    // Order of numbers from top and clockwise around the template
//    private val order = listOf(2, 3, 5, 8, 7, 6, 4, 1)
    private val results = mapOf(
        1 to Direction(-1, -1),
        2 to Direction(0, -1),
        3 to Direction(1, -1),
        4 to Direction(-1, 0),
        5 to Direction(1, 0),
        6 to Direction(-1, 1),
        7 to Direction(0, 1),
        8 to Direction(1, 1)
    )

    /**
     * When the template is placed on the field (and not in a corner), roll
     * a D8 to determine the direction the object is moving in.
     */
    fun roll(roll: D8Result): Direction {
        return results[roll.result] ?:throw IllegalArgumentException("Only values between [1, 8] is allowed: ${roll.result}")
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
    fun roll(corner: CornerThrowInPosition, d3: D3Result): Direction {
        return rotateVector(results[d3.result]!!, corner.rotateDegrees)
    }

    private fun rotateVector(vector: Direction, angleDegrees: Int): Direction {
        // Use the Rotation Matrix to rotate the coordinates
        val angleRadians = angleDegrees * PI / 180.0
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        val x: Double = vector.xModifier * cosTheta - vector.yModifier * sinTheta
        val y = vector.xModifier * sinTheta + vector.yModifier * cosTheta
        return Direction(x.roundToInt(), y.roundToInt())
    }
}
