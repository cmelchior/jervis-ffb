package dk.ilios.analyzer.fumbbl.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max

@Serializable
class FieldCoordinate(
    var x: Int = 0,
    var y: Int = 0
) {
    val isBoxCoordinate: Boolean
        get() {
            when (x) {
                -7, -6, -5, -4, -3, -2, -1, 30, 31, 32, 33, 34, 35, 36 -> return true
            }
            return false
        }

    fun add(deltaX: Int, deltaY: Int): FieldCoordinate {
        return FieldCoordinate(x + deltaX, y + deltaY)
    }

    fun distanceInSteps(otherCoordinate: FieldCoordinate?): Int {
        var result = -1
        if (otherCoordinate != null) {
            result = max(Math.abs(x - otherCoordinate.x), abs(y - otherCoordinate.y))
        }
        return result
    }

    fun isAdjacent(pOtherCoordinate: FieldCoordinate?): Boolean {
        return pOtherCoordinate != null && distanceInSteps(pOtherCoordinate) == 1
    }

    fun transform(): FieldCoordinate {
        when (x) {
            -1 -> return FieldCoordinate(30, y)
            -2 -> return FieldCoordinate(31, y)
            -3 -> return FieldCoordinate(32, y)
            -4 -> return FieldCoordinate(33, y)
            -5 -> return FieldCoordinate(34, y)
            -6 -> return FieldCoordinate(35, y)
            -7 -> return FieldCoordinate(36, y)
            30 -> return FieldCoordinate(-1, y)
            31 -> return FieldCoordinate(-2, y)
            32 -> return FieldCoordinate(-3, y)
            33 -> return FieldCoordinate(-4, y)
            34 -> return FieldCoordinate(-5, y)
            35 -> return FieldCoordinate(-6, y)
            36 -> return FieldCoordinate(-7, y)
        }
        return FieldCoordinate(25 - x, y)
    }
    override fun toString(): String {
        return "(" + x + "," + y + ")"
    }
    operator fun compareTo(pAnotherFc: FieldCoordinate?): Int {
        if (pAnotherFc == null) {
            return -1
        }
        if (pAnotherFc.x < x) {
            return 1
        }
        return if (pAnotherFc.x > x) {
            -1
        } else y - pAnotherFc.y
    }

    fun move(d: Direction, distance: Int): FieldCoordinate {
        val result = FieldCoordinate(x, y)
        var dy = 0
        var dx = 0
        when (d) {
            Direction.NORTH -> dy = -1
            Direction.NORTHEAST -> {
                dx = 1
                dy = -1
            }
            Direction.EAST -> dx = 1
            Direction.SOUTHEAST -> {
                dx = 1
                dy = 1
            }
            Direction.SOUTH -> dy = 1
            Direction.SOUTHWEST -> {
                dx = -1
                dy = 1
            }
            Direction.WEST -> dx = -1
            Direction.NORTHWEST -> {
                dx = -1
                dy = -1
            }
        }
        for (i in 0 until distance) {
            result.x += dx
            result.y += dy
        }
        return result
    }

    companion object {
        const val FIELD_WIDTH = 26
        const val FIELD_HEIGHT = 15
        const val RSV_HOME_X = -1
        const val KO_HOME_X = -2
        const val BH_HOME_X = -3
        const val SI_HOME_X = -4
        const val RIP_HOME_X = -5
        const val BAN_HOME_X = -6
        const val MNG_HOME_X = -7
        const val RSV_AWAY_X = 30
        const val KO_AWAY_X = 31
        const val BH_AWAY_X = 32
        const val SI_AWAY_X = 33
        const val RIP_AWAY_X = 34
        const val BAN_AWAY_X = 35
        const val MNG_AWAY_X = 36

        fun transform(pFieldCoordinate: FieldCoordinate?): FieldCoordinate? {
            return pFieldCoordinate?.transform()
        }

        fun equals(pCoordinate1: FieldCoordinate?, pCoordinate2: FieldCoordinate?): Boolean {
            return if (pCoordinate1 != null) pCoordinate1 == pCoordinate2 else pCoordinate2 == null
        }

        fun getDirection(from: FieldCoordinate, to: FieldCoordinate): Direction? {
            val dx = to.x - from.x
            val dy = to.y - from.y
            if (dx < 0) {
                if (dy < 0) return Direction.NORTHWEST
                return if (dy > 0) {
                    Direction.SOUTHWEST
                } else Direction.WEST
            }
            if (dx > 0) {
                if (dy < 0) return Direction.NORTHEAST
                return if (dy > 0) {
                    Direction.SOUTHEAST
                } else Direction.EAST
            }
            return Direction.NORTH
            return if (dy > 0) {
                Direction.SOUTH
            } else null
        }
    }
}