package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Direction
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */
@Serializable
sealed interface Location {
    val coordinate: FieldCoordinate

    fun isOnLineOfScrimmage(rules: Rules): Boolean

    fun isInWideZone(rules: Rules): Boolean

    fun isInEndZone(rules: Rules): Boolean

    fun isInCenterField(rules: Rules): Boolean

    fun isOnHomeSide(rules: Rules): Boolean

    fun isOnAwaySide(rules: Rules): Boolean

    fun isOnField(rules: Rules): Boolean

    fun isOutOfBounds(rules: Rules): Boolean

    fun isAdjacent(
        rules: Rules,
        location: Location,
    ): Boolean
}

// (0, 0) is (top, left)
@Serializable
data class FieldCoordinate(val x: Int, val y: Int) : Location {
    companion object {
        val UNKNOWN = FieldCoordinate(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    override val coordinate: FieldCoordinate = this

    override fun isOnLineOfScrimmage(rules: Rules): Boolean {
        return x == rules.lineOfScrimmageHome || x == rules.lineOfScrimmageAway
    }

    override fun isInWideZone(rules: Rules): Boolean {
        return (0u until rules.wideZone).contains(y.toUInt()) ||
            (rules.fieldHeight - rules.wideZone until rules.fieldHeight).contains(y.toUInt())
    }

    override fun isInEndZone(rules: Rules): Boolean {
        return x == 0 || x.toUInt() == rules.fieldWidth - 1u
    }

    override fun isInCenterField(rules: Rules): Boolean {
        val xRange = (rules.endZone until rules.fieldWidth - rules.endZone)
        val yRange = (rules.wideZone until rules.fieldHeight - rules.wideZone)
        return xRange.contains(x.toUInt()) && yRange.contains(y.toUInt())
    }

    override fun isOnHomeSide(rules: Rules): Boolean {
        return x.toUInt() < rules.fieldWidth / 2u
    }

    override fun isOnAwaySide(rules: Rules): Boolean {
        return x.toUInt() >= rules.fieldWidth / 2u
    }

    override fun isOnField(rules: Rules): Boolean {
        return (x >= 0 && x < rules.fieldWidth.toInt() && y >= 0 && y < rules.fieldHeight.toInt())
    }

    override fun isOutOfBounds(rules: Rules): Boolean {
        return x < 0 || x >= rules.fieldWidth.toInt() || y < 0 || y >= rules.fieldHeight.toInt()
    }

    override fun isAdjacent(
        rules: Rules,
        location: Location,
    ): Boolean {
        return distanceTo(location.coordinate) == 1u
    }

    fun move(
        direction: Direction,
        steps: Int,
    ): FieldCoordinate {
        return FieldCoordinate(x + (direction.xModifier * steps), y + (direction.yModifier * steps))
    }

    fun toLogString(): String {
        return "[$x, $y]"
    }

    /**
     * Return all on-field coordinates around a specific on-field location.
     */
    fun getSurroundingCoordinates(
        rules: Rules,
        distance: UInt = 1u,
    ): List<FieldCoordinate> {
        val result = mutableListOf<FieldCoordinate>()
        (x - distance.toInt()..x + distance.toInt()).forEach { x: Int ->
            (y - distance.toInt()..y + distance.toInt()).forEach { y: Int ->
                val newCoordinate = FieldCoordinate(x, y)
                if (newCoordinate != this && newCoordinate.isOnField(rules)) {
                    result.add(newCoordinate)
                }
            }
        }
        return result
    }

    /**
     * Returns the Chebyshev Distance between this field and the target location.
     * This is equal to the minimum number of squares between two squares on the game field, if we assume
     * that the field is a square.
     *
     * See https://en.wikipedia.org/wiki/Chebyshev_distance
     */
    fun distanceTo(target: FieldCoordinate): UInt {
        return max(abs(target.x - this.x), abs(target.y - this.y)).toUInt()
    }

    /**
     * Returns the "real" distance between two fields, as if they were points in a coordinate system
     * This means that unlike [distanceTo] diagonals will have a larger value than squares on a line.
     */
    fun realDistanceTo(target: FieldCoordinate): Double {
        return sqrt((target.x - x).toDouble().pow(2) + (target.y - y).toDouble().pow(2))
    }

    /**
     * Returns `true` if a field is diagonal to another, false if they are not.
     * This only works on two fields next to each other.
     */
    fun isDiagonalTo(target: FieldCoordinate): Boolean {
        val onLine = (x - target.x == 0 || y - target.y == 0)
        return !onLine
    }

    /**
     * Return all coordinates that are considered "away" from this coordinate from the point of view of the provided
     * [location].
     *
     * See page 45 in the rulebook.
     */
    fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: FieldCoordinate,
        includeOutOfBounds: Boolean = false,
    ): List<FieldCoordinate> {
        // Calculate direction
        val direction = Direction(this.x - location.x, this.y - location.y)

        val allCoordinates: List<FieldCoordinate> =
            when {
                // Top
                direction.xModifier == 0 && direction.yModifier == -1 ->
                    listOf(
                        FieldCoordinate(this.x - 1, this.y - 1),
                        FieldCoordinate(this.x, this.y - 1),
                        FieldCoordinate(this.x + 1, this.y - 1),
                    )
                // Bottom
                direction.xModifier == 0 && direction.yModifier == 1 ->
                    listOf(
                        FieldCoordinate(this.x - 1, this.y + 1),
                        FieldCoordinate(this.x, this.y + 1),
                        FieldCoordinate(this.x + 1, this.y + 1),
                    )
                // Left
                direction.xModifier == -1 && direction.yModifier == 0 ->
                    listOf(
                        FieldCoordinate(this.x - 1, this.y - 1),
                        FieldCoordinate(this.x - 1, this.y + 0),
                        FieldCoordinate(this.x - 1, this.y + 1),
                    )
                // Right
                direction.xModifier == 1 && direction.yModifier == 0 ->
                    listOf(
                        FieldCoordinate(this.x + 1, this.y - 1),
                        FieldCoordinate(this.x + 1, this.y + 0),
                        FieldCoordinate(this.x + 1, this.y + 1),
                    )
                // Top-left
                direction.xModifier == -1 && direction.yModifier == -1 ->
                    listOf(
                        FieldCoordinate(this.x - 1, this.y),
                        FieldCoordinate(this.x - 1, this.y - 1),
                        FieldCoordinate(this.x, this.y - 1),
                    )
                // Top-right
                direction.xModifier == 1 && direction.yModifier == -1 ->
                    listOf(
                        FieldCoordinate(this.x, this.y - 1),
                        FieldCoordinate(this.x + 1, this.y - 1),
                        FieldCoordinate(this.x + 1, this.y),
                    )
                // Bottom-left
                direction.xModifier == -1 && direction.yModifier == 1 ->
                    listOf(
                        FieldCoordinate(this.x - 1, this.y),
                        FieldCoordinate(this.x - 1, this.y + 1),
                        FieldCoordinate(this.x, this.y + 1),
                    )
                // Bottom-Right
                direction.xModifier == 1 && direction.yModifier == 1 ->
                    listOf(
                        FieldCoordinate(this.x + 1, this.y),
                        FieldCoordinate(this.x + 1, this.y + 1),
                        FieldCoordinate(this.x, this.y + 1),
                    )
                else -> throw IllegalArgumentException("Unsupported direction: $direction")
            }
        return if (!includeOutOfBounds) {
            allCoordinates.filter {
                it.isOnField(rules)
            }
        } else {
            allCoordinates
        }
    }
}

data object DogOut : Location {
    override val coordinate: FieldCoordinate = FieldCoordinate.UNKNOWN

    override fun isOnLineOfScrimmage(rules: Rules): Boolean = false

    override fun isInWideZone(rules: Rules): Boolean = false

    override fun isInEndZone(rules: Rules): Boolean = false

    override fun isInCenterField(rules: Rules): Boolean = false

    override fun isOnHomeSide(rules: Rules): Boolean = false

    override fun isOnAwaySide(rules: Rules): Boolean = false

    override fun isOnField(rules: Rules): Boolean = false

    override fun isOutOfBounds(rules: Rules): Boolean = false

    override fun isAdjacent(
        rules: Rules,
        location: Location,
    ): Boolean = false
}
