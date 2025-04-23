package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.CornerThrowInPosition

/**
 * This class represents a Giants location on the board.
 *
 * See page 54 in Death Zone.
 */
class GiantLocation(val coordinates: MutableList<FieldCoordinate>): OnFieldLocation {

    override fun isOnLineOfScrimmage(rules: Rules): Boolean {
        TODO()
        //return x == rules.lineOfScrimmageHome || x == rules.lineOfScrimmageAway
    }

    override fun isInWideZone(rules: Rules): Boolean {
        TODO()
//        return (0 until rules.wideZone).contains(y) ||
//            (rules.fieldHeight - rules.wideZone until rules.fieldHeight).contains(y)
    }

    override fun isInEndZone(rules: Rules): Boolean {
        TODO()
        // return x == 0 || x == rules.fieldWidth - 1
    }

    override fun isInCenterField(rules: Rules): Boolean {
        TODO()
//        val xRange = (rules.endZone until rules.fieldWidth - rules.endZone)
//        val yRange = (rules.wideZone until rules.fieldHeight - rules.wideZone)
//        return xRange.contains(x) && yRange.contains(y)
    }

    override fun isInNoMansLand(rules: Rules): Boolean {
        TODO()
    }

    override fun isOnHomeSide(rules: Rules): Boolean {
        TODO()
//        return x < rules.fieldWidth / 2
    }

    override fun isOnAwaySide(rules: Rules): Boolean {
        TODO()
//        return x >= rules.fieldWidth / 2
    }

    override fun isOnField(rules: Rules): Boolean {
        TODO()
//        return (x >= 0 && x < rules.fieldWidth && y >= 0 && y < rules.fieldHeight)
    }

    override fun isOutOfBounds(rules: Rules): Boolean {
        TODO()
//        return x < 0 || x >= rules.fieldWidth || y < 0 || y >= rules.fieldHeight
    }

    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? {
        TODO()
//        return when {
//            x == 0 && y == 0 -> CornerThrowInPosition.TOP_LEFT
//            x == 0 && y == rules.fieldHeight - 1 -> CornerThrowInPosition.BOTTOM_LEFT
//            x == rules.fieldWidth -1 && y == 0 -> CornerThrowInPosition.TOP_RIGHT
//            x == rules.fieldWidth - 1 && y == rules.fieldHeight -1 -> CornerThrowInPosition.BOTTOM_RIGHT
//            else -> null
//        }
    }

    override fun isAdjacent(rules: Rules, location: Location): Boolean {
        TODO()
//        return distanceTo(location) == 1
    }

    override fun overlap(otherLocation: Location): Boolean {
        TODO()
//        return when (otherLocation) {
//            DogOut -> false
//            is FieldCoordinate -> otherLocation.x == x || otherLocation.y == y
//        }
    }

    override fun move(
        direction: Direction,
        steps: Int,
    ): FieldCoordinate {
        TODO()
        //        return create(x + (direction.xModifier * steps), y + (direction.yModifier * steps))
    }

    override fun toLogString(): String {
        TODO()
//        return "[${x+1}, ${y+1}]"
    }

    /**
     * Return all on-field coordinates around a specific on-field location.
     * TODO Figure out if coordinates out of bounds should be returned as that or as their real coordinate
     * value (to make it easier to make calculations)
     */
    override fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int,
        includeOutOfBounds: Boolean,
    ): List<FieldCoordinate> {
        TODO()
//        val result = mutableListOf<FieldCoordinate>()
//        (x - distance..x + distance).forEach { x: Int ->
//            (y - distance..y + distance).forEach { y: Int ->
//                val newCoordinate = create(x, y)
//                if (newCoordinate.isOnField(rules) && this != newCoordinate) {
//                    result.add(newCoordinate)
//                }
//                if (includeOutOfBounds && newCoordinate.isOutOfBounds(rules)) {
//                    result.add(newCoordinate)
//                }
//            }
//        }
//        return result
    }

    /**
     * Returns the Chebyshev Distance between this field and the target location.
     * This is equal to the minimum number of squares between two squares on the game field, if we assume
     * that the field is a square.
     *
     * See https://en.wikipedia.org/wiki/Chebyshev_distance
     */
    override fun distanceTo(target: OnFieldLocation): Int {
        TODO()
//        return when (target) {
//            is FieldCoordinate -> max(abs(target.x - this.x), abs(target.y - this.y))
//        }
    }

    /**
     * Returns the "real" distance between two fields, as if they were points in a coordinate system
     * This means that unlike [distanceTo] diagonals will have a larger value than squares on a line.
     */
    override fun realDistanceTo(target: OnFieldLocation): Double {
        TODO()
//        return sqrt((target.x - x).toDouble().pow(2) + (target.y - y).toDouble().pow(2))
    }

    /**
     * Returns `true` if a field is diagonal to another, false if they are not.
     * This only works on two fields next to each other.
     */
    override fun isDiagonalTo(target: OnFieldLocation): Boolean {
        TODO()
//        val onLine = (x - target.x == 0 || y - target.y == 0)
//        return !onLine
    }

    /**
     * Return all coordinates that are considered "away" from this coordinate from the point of view of the provided
     * [location].
     *
     * See page 45 in the rulebook.
     */
    override fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: FieldCoordinate,
        includeOutOfBounds: Boolean,
    ): List<FieldCoordinate> {
        TODO()
//        // Calculate direction
//        val direction = Direction(this.x - location.x, this.y - location.y)
//
//        val allCoordinates: List<FieldCoordinate> =
//            when {
//                // Top
//                direction.xModifier == 0 && direction.yModifier == -1 ->
//                    listOf(
//                        create(this.x - 1, this.y - 1),
//                        create(this.x, this.y - 1),
//                        create(this.x + 1, this.y - 1),
//                    )
//                // Bottom
//                direction.xModifier == 0 && direction.yModifier == 1 ->
//                    listOf(
//                        create(this.x - 1, this.y + 1),
//                        create(this.x, this.y + 1),
//                        create(this.x + 1, this.y + 1),
//                    )
//                // Left
//                direction.xModifier == -1 && direction.yModifier == 0 ->
//                    listOf(
//                        create(this.x - 1, this.y - 1),
//                        create(this.x - 1, this.y + 0),
//                        create(this.x - 1, this.y + 1),
//                    )
//                // Right
//                direction.xModifier == 1 && direction.yModifier == 0 ->
//                    listOf(
//                        create(this.x + 1, this.y - 1),
//                        create(this.x + 1, this.y + 0),
//                        create(this.x + 1, this.y + 1),
//                    )
//                // Top-left
//                direction.xModifier == -1 && direction.yModifier == -1 ->
//                    listOf(
//                        create(this.x - 1, this.y),
//                        create(this.x - 1, this.y - 1),
//                        create(this.x, this.y - 1),
//                    )
//                // Top-right
//                direction.xModifier == 1 && direction.yModifier == -1 ->
//                    listOf(
//                        create(this.x, this.y - 1),
//                        create(this.x + 1, this.y - 1),
//                        create(this.x + 1, this.y),
//                    )
//                // Bottom-left
//                direction.xModifier == -1 && direction.yModifier == 1 ->
//                    listOf(
//                        create(this.x - 1, this.y),
//                        create(this.x - 1, this.y + 1),
//                        create(this.x, this.y + 1),
//                    )
//                // Bottom-Right
//                direction.xModifier == 1 && direction.yModifier == 1 ->
//                    listOf(
//                        create(this.x + 1, this.y),
//                        create(this.x + 1, this.y + 1),
//                        create(this.x, this.y + 1),
//                    )
//                else -> throw IllegalArgumentException("Unsupported direction: $direction")
//            }
//        return if (!includeOutOfBounds) {
//            allCoordinates.filter {
//                it.isOnField(rules)
//            }
//        } else {
//            allCoordinates
//        }
    }

    // Swap the coordinates around the Y axis
    // TODO Figure out exactly where/how it is best to do this
    fun swapX(rules: Rules): FieldCoordinate {
        TODO()
//        rules.fieldWidth
//        return create(rules.fieldWidth - x - 1, y)
    }
}
