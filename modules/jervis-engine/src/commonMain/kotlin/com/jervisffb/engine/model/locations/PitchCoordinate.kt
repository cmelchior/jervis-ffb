package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.CornerThrowInPosition
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Helper function, making it easier to create a new PitchCoordinate directly
 * from the interface.
 */
fun PitchCoordinate(x: Int, y: Int): PitchCoordinate {
    return PitchCoordinate.create(x, y)
}

/**
 * A representation of the coordinates for a field square.
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */
interface PitchCoordinate: OnPitchLocation {
    val x: Int
    val y: Int

    override fun isOnLineOfScrimmage(rules: Rules): Boolean {
        if (x < 0 || x >= rules.pitchWidth) return false
        return x == rules.lineOfScrimmageHome || x == rules.lineOfScrimmageAway
    }

    // Page 23 in BB2025 defines the Wide Zone to run across the entire length
    // of the pitch.
    override fun isInWideZone(rules: Rules): Boolean {
        if (x < 0 || x >= rules.pitchWidth) return false
        return (0 until rules.wideZone).contains(y) ||
            (rules.pitchHeight - rules.wideZone until rules.pitchHeight).contains(y)
    }

    override fun isInEndZone(rules: Rules): Boolean {
        if (y < 0 || y >= rules.pitchHeight) return false
        return x < rules.endZone || x >= rules.pitchWidth - rules.endZone
    }

    override fun isInCenterField(rules: Rules): Boolean {
        val xRange = (rules.endZone until rules.pitchWidth - rules.endZone)
        val yRange = (rules.wideZone until rules.pitchHeight - rules.wideZone)
        return xRange.contains(x) && yRange.contains(y)
    }

    override fun isInNoMansLand(rules: Rules): Boolean {
        if (y < 0 || y >= rules.pitchHeight) return false
        return x > rules.lineOfScrimmageHome && x < rules.lineOfScrimmageAway
    }

    // "Home-side" is a bit vague and needs to be clarified
    override fun isOnHomeSide(rules: Rules): Boolean {
        if (y < 0 || y >= rules.pitchHeight) return false
        return x < rules.pitchWidth / 2
    }

    // "Away-side" is a bit vague and needs to be clarified
    override fun isOnAwaySide(rules: Rules): Boolean {
        if (y < 0 || y >= rules.pitchHeight) return false
        return x >= rules.pitchWidth / 2
    }

    override fun isOnPitch(rules: Rules): Boolean {
        return (x >= 0 && x < rules.pitchWidth && y >= 0 && y < rules.pitchHeight)
    }

    override fun isOutOfBounds(rules: Rules): Boolean {
        return x < 0 || x >= rules.pitchWidth || y < 0 || y >= rules.pitchHeight
    }

    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? {
        return when {
            x == 0 && y == 0 -> CornerThrowInPosition.TOP_LEFT
            x == 0 && y == rules.pitchHeight - 1 -> CornerThrowInPosition.BOTTOM_LEFT
            x == rules.pitchWidth -1 && y == 0 -> CornerThrowInPosition.TOP_RIGHT
            x == rules.pitchWidth - 1 && y == rules.pitchHeight - 1 -> CornerThrowInPosition.BOTTOM_RIGHT
            else -> null
        }
    }

    override fun isAdjacent(rules: Rules, location: Location): Boolean {
        return when (location) {
            DogOut -> false
            is PitchCoordinate -> distanceTo(location) == 1
            is GiantLocation -> distanceTo(location) == 1
        }
    }

    override fun overlap(otherLocation: Location): Boolean {
        return when (otherLocation) {
            DogOut -> false
            is PitchCoordinate -> otherLocation.x == x && otherLocation.y == y
            is GiantLocation -> otherLocation.coordinates.contains(PitchCoordinate(x, y))
        }
    }

    override fun move(
        direction: Direction,
        steps: Int,
    ): PitchCoordinate {
        return create(x + (direction.xModifier * steps), y + (direction.yModifier * steps))
    }

    override fun toLogString(): String {
        return "[${x+1}, ${y+1}]"
    }

    override fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int,
        includeOutOfBounds: Boolean,
    ): List<PitchCoordinate> {
        val result = mutableListOf<PitchCoordinate>()
        (x - distance..x + distance).forEach { x: Int ->
            (y - distance..y + distance).forEach { y: Int ->
                val newCoordinate = create(x, y)
                if (newCoordinate.isOnPitch(rules) && this != newCoordinate) {
                    result.add(newCoordinate)
                }
                if (includeOutOfBounds && newCoordinate.isOutOfBounds(rules)) {
                    result.add(newCoordinate)
                }
            }
        }
        return result
    }

    override fun distanceTo(target: OnPitchLocation): Int {
        return when (target) {
            is PitchCoordinate -> max(abs(target.x - this.x), abs(target.y - this.y))
            is GiantLocation -> target.coordinates.minOf { distanceTo(it) }
        }
    }

    override fun realDistanceTo(target: OnPitchLocation): Double {
        return when (target) {
            is PitchCoordinate -> sqrt((target.x - x).toDouble().pow(2) + (target.y - y).toDouble().pow(2))
            is GiantLocation -> TODO()
        }
    }

    override fun isDiagonalTo(target: OnPitchLocation): Boolean {
        return when (target) {
            is PitchCoordinate -> {
                val onLine = (x - target.x == 0 || y - target.y == 0)
                !onLine
            }
            is GiantLocation -> TODO()
        }
    }

    override fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: PitchCoordinate,
        includeOutOfBounds: Boolean,
    ): List<PitchCoordinate> {
        // Calculate direction
        val direction = Direction(this.x - location.x, this.y - location.y)
        val allCoordinates: List<PitchCoordinate> =
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
                it.isOnPitch(rules)
            }
        } else {
            allCoordinates
        }
    }

    companion object {
        val UNKNOWN = create(Int.MAX_VALUE, Int.MAX_VALUE)
        fun create(x: Int, y: Int): PitchCoordinate {
            return PitchCoordinateImpl(x, y)
        }
    }
}

data class PitchCoordinateImpl(override val x: Int, override val y: Int) : PitchCoordinate
