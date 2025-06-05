package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Range Ruler
 *
 * See page 48 in the rulebook.
 */
@Serializable
object RangeRuler {

    // Max distance that can be thrown.
    val maxDistance: Int = 13
    // Width of the range ruler as a scale factor compared to a square on the board.
    val rulerWidth: Double = 1.74

    // Represent the template as a string
    // Credit for this idea: https://github.com/christerk/ffb/blob/660b0e10357b10634827b4ed787f21cc9757b0c2/ffb-common/src/main/java/com/fumbbl/ffb/mechanics/bb2020/PassMechanic.java#L22
    val TEMPlATE = """
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

    /**
     * Measure the range between a [thrower] and a target square as if using a Range Ruler
     * as described by the rulebook on page 48.
     *
     * Will throw an error if the player is not on the field.
     */
    fun measure(thrower: Player, target: FieldCoordinate): Range = measure(thrower.coordinates, target)

    /**
     * Measure the range between two points on the field as if using a Range Ruler as
     * described by the rulebook on page 48.
     */
    fun measure(origin: FieldCoordinate, target: FieldCoordinate): Range {
        val deltaX: Int = abs(target.x - origin.x)
        val deltaY: Int = abs(target.y - origin.y)
        return if ((deltaX < ranges.size) && (deltaY < ranges[deltaX].size)) {
            ranges[deltaX][deltaY]
        } else {
            Range.OUT_OF_RANGE
        }
    }

    /**
     * Return all players of the opposite team that are considered "under the ruler". [thrower] and
     * [target] are not included. This is used for deflecting and intercepting balls and bombs.
     */
    fun opponentPlayersUnderRuler(thrower: Player, target: FieldCoordinate): List<Player> {
        TODO()
    }

//    fun findInterceptors(pGame: Game, pThrower: Player<*>?, pTargetCoordinate: FieldCoordinate?): Array<Player<*>?> {
//        val interceptors: MutableList<Player<*>?> = java.util.ArrayList<Player<*>?>()
//        if ((pTargetCoordinate != null) && (pThrower != null)) {
//            val throwerCoordinate: FieldCoordinate = pGame.getFieldModel().getPlayerCoordinate(pThrower)
//            val otherTeam: Team = if (pGame.getTeamHome().hasPlayer(pThrower)) pGame.getTeamAway() else pGame.getTeamHome()
//            val otherPlayers: Array<Player<*>> = otherTeam.getPlayers()
//            for (otherPlayer in otherPlayers) {
//                val interceptorState: PlayerState? = pGame.getFieldModel().getPlayerState(otherPlayer)
//                val interceptorCoordinate: FieldCoordinate? = pGame.getFieldModel().getPlayerCoordinate(otherPlayer)
//                if ((interceptorCoordinate != null) && (interceptorState != null) && interceptorState.hasTacklezones()
//                    && !otherPlayer.hasSkillProperty(NamedProperties.preventCatch)
//                ) {
//                    if (canIntercept(throwerCoordinate, pTargetCoordinate, interceptorCoordinate)) {
//                        interceptors.add(otherPlayer)
//                    }
//                }
//            }
//        }
//        return interceptors.toTypedArray<Player?>()
//    }
//
//    private fun canIntercept(
//        pThrowerCoordinate: FieldCoordinate, pTargetCoordinate: FieldCoordinate,
//        pIinterceptorCoordinate: FieldCoordinate
//    ): Boolean {
//        val receiverX: Int = pTargetCoordinate.getX() - pThrowerCoordinate.getX()
//        val receiverY: Int = pTargetCoordinate.getY() - pThrowerCoordinate.getY()
//        val interceptorX: Int = pIinterceptorCoordinate.getX() - pThrowerCoordinate.getX()
//        val interceptorY: Int = pIinterceptorCoordinate.getY() - pThrowerCoordinate.getY()
//        val a = (((receiverX - interceptorX) * (receiverX - interceptorX))
//            + ((receiverY - interceptorY) * (receiverY - interceptorY)))
//        val b = (interceptorX * interceptorX) + (interceptorY * interceptorY)
//        val c = (receiverX * receiverX) + (receiverY * receiverY)
//        val d1 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY + 0.5)))
//        val d2 = abs((receiverY * (interceptorX + 0.5)) - (receiverX * (interceptorY - 0.5)))
//        val d3 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY + 0.5)))
//        val d4 = abs((receiverY * (interceptorX - 0.5)) - (receiverX * (interceptorY - 0.5)))
//        return (c > a) && (c > b) && (RULER_WIDTH > (2 * min(min(min(d1, d2), d3), d4) / sqrt(c.toDouble())))
//    }
}
