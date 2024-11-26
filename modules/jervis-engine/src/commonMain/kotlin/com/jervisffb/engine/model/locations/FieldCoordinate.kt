package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.CornerThrowInPosition
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Helper function, making it easier to create FieldCoordinate from the interface.
 */
fun FieldCoordinate(x: Int, y: Int): FieldCoordinate {
    return FieldCoordinate.create(x, y)
}

/**
 * A representation of the coordinates for a field square.
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */
interface FieldCoordinate: OnFieldLocation {
    val x: Int
    val y: Int

    override fun isOnLineOfScrimmage(rules: Rules): Boolean {
        return x == rules.lineOfScrimmageHome || x == rules.lineOfScrimmageAway
    }

    override fun isInWideZone(rules: Rules): Boolean {
        return (0 until rules.wideZone).contains(y) ||
            (rules.fieldHeight - rules.wideZone until rules.fieldHeight).contains(y)
    }

    override fun isInEndZone(rules: Rules): Boolean {
        return x == 0 || x == rules.fieldWidth - 1
    }

    override fun isInCenterField(rules: Rules): Boolean {
        val xRange = (rules.endZone until rules.fieldWidth - rules.endZone)
        val yRange = (rules.wideZone until rules.fieldHeight - rules.wideZone)
        return xRange.contains(x) && yRange.contains(y)
    }

    override fun isOnHomeSide(rules: Rules): Boolean {
        return x < rules.fieldWidth / 2
    }

    override fun isOnAwaySide(rules: Rules): Boolean {
        return x >= rules.fieldWidth / 2
    }

    override fun isOnField(rules: Rules): Boolean {
        return (x >= 0 && x < rules.fieldWidth && y >= 0 && y < rules.fieldHeight)
    }

    override fun isOutOfBounds(rules: Rules): Boolean {
        return x < 0 || x >= rules.fieldWidth || y < 0 || y >= rules.fieldHeight
    }

    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? {
        return when {
            x == 0 && y == 0 -> CornerThrowInPosition.TOP_LEFT
            x == 0 && y == rules.fieldHeight - 1 -> CornerThrowInPosition.BOTTOM_LEFT
            x == rules.fieldWidth -1 && y == 0 -> CornerThrowInPosition.TOP_RIGHT
            x == rules.fieldWidth - 1 && y == rules.fieldHeight -1 -> CornerThrowInPosition.BOTTOM_RIGHT
            else -> null
        }
    }

    override fun isAdjacent(rules: Rules, location: Location): Boolean {
        return when (location) {
            DogOut -> false
            is FieldCoordinate -> distanceTo(location) == 1
            is GiantLocation -> distanceTo(location) == 1
        }
    }

    override fun overlap(otherLocation: Location): Boolean {
        return when (otherLocation) {
            DogOut -> false
            is FieldCoordinate -> otherLocation.x == x && otherLocation.y == y
            is GiantLocation -> otherLocation.coordinates.contains(FieldCoordinate(x, y))
        }
    }

    override fun move(
        direction: Direction,
        steps: Int,
    ): FieldCoordinate {
        return create(x + (direction.xModifier * steps), y + (direction.yModifier * steps))
    }

    override fun toLogString(): String {
        return "[${x+1}, ${y+1}]"
    }

    override fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int,
        includeOutOfBounds: Boolean,
    ): List<FieldCoordinate> {
        val result = mutableListOf<FieldCoordinate>()
        (x - distance..x + distance).forEach { x: Int ->
            (y - distance..y + distance).forEach { y: Int ->
                val newCoordinate = create(x, y)
                if (newCoordinate.isOnField(rules) && this != newCoordinate) {
                    result.add(newCoordinate)
                }
                if (includeOutOfBounds && newCoordinate.isOutOfBounds(rules)) {
                    result.add(newCoordinate)
                }
            }
        }
        return result
    }

    override fun distanceTo(target: OnFieldLocation): Int {
        return when (target) {
            is FieldCoordinate -> max(abs(target.x - this.x), abs(target.y - this.y))
            is GiantLocation -> target.coordinates.minOf { distanceTo(it) }
        }
    }

    override fun realDistanceTo(target: OnFieldLocation): Double {
        return when (target) {
            is FieldCoordinate -> sqrt((target.x - x).toDouble().pow(2) + (target.y - y).toDouble().pow(2))
            is GiantLocation -> TODO()
        }
    }

    override fun isDiagonalTo(target: OnFieldLocation): Boolean {
        return when (target) {
            is FieldCoordinate -> {
                val onLine = (x - target.x == 0 || y - target.y == 0)
                !onLine
            }
            is GiantLocation -> TODO()
        }
    }

    override fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: FieldCoordinate,
        includeOutOfBounds: Boolean,
    ): List<FieldCoordinate> {
        // Calculate direction
        val direction = Direction(this.x - location.x, this.y - location.y)
        val allCoordinates: List<FieldCoordinate> =
            when {
                // Top
                direction.xModifier == 0 && direction.yModifier == -1 ->
                    listOf(
                        create(this.x - 1, this.y - 1),
                        create(this.x, this.y - 1),
                        create(this.x + 1, this.y - 1),
                    )
                // Bottom
                direction.xModifier == 0 && direction.yModifier == 1 ->
                    listOf(
                        create(this.x - 1, this.y + 1),
                        create(this.x, this.y + 1),
                        create(this.x + 1, this.y + 1),
                    )
                // Left
                direction.xModifier == -1 && direction.yModifier == 0 ->
                    listOf(
                        create(this.x - 1, this.y - 1),
                        create(this.x - 1, this.y + 0),
                        create(this.x - 1, this.y + 1),
                    )
                // Right
                direction.xModifier == 1 && direction.yModifier == 0 ->
                    listOf(
                        create(this.x + 1, this.y - 1),
                        create(this.x + 1, this.y + 0),
                        create(this.x + 1, this.y + 1),
                    )
                // Top-left
                direction.xModifier == -1 && direction.yModifier == -1 ->
                    listOf(
                        create(this.x - 1, this.y),
                        create(this.x - 1, this.y - 1),
                        create(this.x, this.y - 1),
                    )
                // Top-right
                direction.xModifier == 1 && direction.yModifier == -1 ->
                    listOf(
                        create(this.x, this.y - 1),
                        create(this.x + 1, this.y - 1),
                        create(this.x + 1, this.y),
                    )
                // Bottom-left
                direction.xModifier == -1 && direction.yModifier == 1 ->
                    listOf(
                        create(this.x - 1, this.y),
                        create(this.x - 1, this.y + 1),
                        create(this.x, this.y + 1),
                    )
                // Bottom-Right
                direction.xModifier == 1 && direction.yModifier == 1 ->
                    listOf(
                        create(this.x + 1, this.y),
                        create(this.x + 1, this.y + 1),
                        create(this.x, this.y + 1),
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

    // Swap the coordinates around the Y axis
    // TODO Figure out exactly where/how it is best to do this
    fun swapX(rules: Rules): FieldCoordinate {
        rules.fieldWidth
        return create(rules.fieldWidth - x - 1, y)
    }

    companion object {
        val UNKNOWN = create(Int.MAX_VALUE, Int.MAX_VALUE)
        val OUT_OF_BOUNDS = create(Int.MIN_VALUE, Int.MIN_VALUE)
        fun create(x: Int, y: Int): FieldCoordinate {
            return FieldCoordinateImpl(x, y)
        }
    }
}

@Serializable
data class FieldCoordinateImpl(override val x: Int, override val y: Int) : FieldCoordinate
