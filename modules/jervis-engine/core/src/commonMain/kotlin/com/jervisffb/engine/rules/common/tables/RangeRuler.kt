package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate

/**
 * Interface representing a Range Ruler used for throwing balls, bombs and players.
 *
 * See page 48 in the BB 2020 rulebook.
 */
interface RangeRuler {

    // Max distance that can be thrown.
    val MAX_DISTANCE: Int

    /**
     * Measure the range between a [thrower] and a target square.
     *
     * Will throw an error if the player is not on the pitch.
     */
    fun measure(thrower: Player, target: PitchCoordinate): Range

    /**
     * Measure the range between two points on the pitch as if using a Range Ruler.
     */
    fun measure(origin: PitchCoordinate, target: PitchCoordinate): Range

    /**
     * Return all players of the opposite team that are considered "under the ruler". [thrower] and
     * [target] are not included. This is used for deflecting and intercepting balls and bombs.
     */
    fun opponentPlayersUnderRuler(thrower: Player, target: PitchCoordinate): List<Player>
}
