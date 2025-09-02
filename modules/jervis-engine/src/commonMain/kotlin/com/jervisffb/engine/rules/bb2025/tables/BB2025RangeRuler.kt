package com.jervisffb.engine.rules.bb2025.tables

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.rules.common.tables.RangeRuler
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Range Ruler
 *
 * See page XX in the BB 2025 rulebook.
 */
@Serializable
object BB2025RangeRuler: RangeRuler {

    // Max distance that can be thrown.
    override val MAX_DISTANCE: Int = 13
    // Width of the range ruler as a scale factor compared to a square on the board.
    // Square size: 34 mm, Ruler width: 59 mm
    private const val RULER_WIDTH: Double = 1.74

    // Represent the template as a string
    // Credit for this idea: https://github.com/christerk/ffb/blob/660b0e10357b10634827b4ed787f21cc9757b0c2/ffb-common/src/main/java/com/fumbbl/ffb/mechanics/bb2020/PassMechanic.java#L22
    private val TEMPlATE = """
        T Q Q Q S S S L L L L B B B
        Q Q Q Q S S S L L L L B B B
        Q Q Q S S S S L L L L B B
        Q Q S S S S S L L L B B B
        S S S S S S L L L L B B B
        S S S S S L L L L B B B
        S S S S L L L L L B B B
        L L L L L L L L B B B
        L L L L L L L B B B B
        L L L L L B B B B B
        L L L B B B B B B
        B B B B B B B
        B B B B B
        B B
    """.trimIndent()

    private val ranges: Array<Array<Range>>

    init {
        // Assume the template is symmetrical
        val lines = TEMPlATE.lines()
        ranges = Array(lines.size) { y: Int ->
            Array(lines.size) { x: Int ->
                when (val type: Char? = lines[y].getOrNull(x * 2)) {
                    'T' -> Range.PASSING_PLAYER
                    'Q' -> Range.QUICK_PASS
                    'S' -> Range.SHORT_PASS
                    'L' -> Range.LONG_PASS
                    'B' -> Range.LONG_BOMB
                    null -> Range.OUT_OF_RANGE
                    else -> error("Unknown range type: '$type' ($x, $y)")
                }
            }
        }
    }

    override fun measure(thrower: Player, target: FieldCoordinate): Range = measure(thrower.coordinates, target)

    override fun measure(origin: FieldCoordinate, target: FieldCoordinate): Range {
        val deltaX: Int = abs(target.x - origin.x)
        val deltaY: Int = abs(target.y - origin.y)
        return if ((deltaX < ranges.size) && (deltaY < ranges[deltaX].size)) {
            ranges[deltaX][deltaY]
        } else {
            Range.OUT_OF_RANGE
        }
    }

    override fun opponentPlayersUnderRuler(thrower: Player, target: FieldCoordinate): List<Player> {
        val rules = thrower.team.game.rules
        val opponentPlayers = mutableListOf<Player>()

        // It is quicker to search through players on the field, rather than trying to calculate
        // the squares under the ruler, so lets do that.
        thrower.team.otherTeam().forEach { player ->
            // Ignore any player at the the target location
            if (!player.location.overlap(target)) {
                if (rules.canDeflect(player) && isUnderRuler(player, thrower.coordinates, target)) {
                    opponentPlayers.add(player)
                }
            }
        }
        return opponentPlayers
    }

    // Credit for this idea: https://github.com/christerk/ffb/blob/3c084704c1a72ce1c64b3245429717b83f164af0/ffb-common/src/main/java/com/fumbbl/ffb/util/UtilPassing.java#L43
    private fun isUnderRuler(player: Player, start: FieldCoordinate, target: FieldCoordinate): Boolean {
        val receiverX: Int = target.x - start.x
        val receiverY: Int = target.y - start.y
        val interceptorX: Int = player.coordinates.x - start.x
        val interceptorY: Int = player.coordinates.y - start.y
        val a = (((receiverX - interceptorX) * (receiverX - interceptorX)) + ((receiverY - interceptorY) * (receiverY - interceptorY)))
        val b = (interceptorX * interceptorX) + (interceptorY * interceptorY)
        val c = (receiverX * receiverX) + (receiverY * receiverY)
        val d1 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY + 0.5)))
        val d2 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY - 0.5)))
        val d3 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY + 0.5)))
        val d4 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY - 0.5)))
        return (c > a) && (c > b) && (RULER_WIDTH > (2 * min(min(min(d1, d2), d3), d4) / sqrt(c.toDouble())))
    }
}
