package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.CornerThrowInPosition

/**
 * This class represents a Giants location on the board.
 *
 * See page 54 in BB2020 Death Zone.
 */
class GiantLocation(val coordinates: MutableList<PitchCoordinate>): OnPitchLocation {

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

    override fun isOnPitch(rules: Rules): Boolean {
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
//            Dogout -> false
//            is FieldCoordinate -> otherLocation.x == x || otherLocation.y == y
//        }
    }

    override fun move(
        direction: Direction,
        steps: Int,
    ): PitchCoordinate {
        TODO()
        //        return create(x + (direction.xModifier * steps), y + (direction.yModifier * steps))
    }

    override fun toLogString(): String {
        TODO()
//        return "[${x+1}, ${y+1}]"
    }

    override fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int,
        includeOutOfBounds: Boolean,
    ): List<PitchCoordinate> {
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

    override fun distanceTo(target: OnPitchLocation): Int {
        TODO()
//        return when (target) {
//            is FieldCoordinate -> max(abs(target.x - this.x), abs(target.y - this.y))
//        }
    }

    override fun realDistanceTo(target: OnPitchLocation): Double {
        TODO()
//        return sqrt((target.x - x).toDouble().pow(2) + (target.y - y).toDouble().pow(2))
    }
    override fun isDiagonalTo(target: OnPitchLocation): Boolean {
        TODO()
//        val onLine = (x - target.x == 0 || y - target.y == 0)
//        return !onLine
    }

    override fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: PitchCoordinate,
        includeOutOfBounds: Boolean,
    ): List<PitchCoordinate> {
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
}
