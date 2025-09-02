package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.KickOffEvent
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Kick-Off Event Table on page 41 in the rulebook.
 */
@Serializable
object BB7KickOffEventTable: KickOffTable {
    override val name: String = "BB7 Kick-Off Table"
    private val table =
        mapOf(
            2 to KickOffEvent.GET_THE_REF,
            3 to KickOffEvent.TIME_OUT_BB7,
            4 to KickOffEvent.SOLID_DEFENSE_BB7,
            5 to KickOffEvent.HIGH_KICK,
            6 to KickOffEvent.CHEERING_FANS,
            7 to KickOffEvent.BRILLIANT_COACHING,
            8 to KickOffEvent.CHANGING_WEATHER,
            9 to KickOffEvent.QUICK_SNAP_BB7,
            10 to KickOffEvent.BLITZ_BB7,
            11 to KickOffEvent.OFFICIOUS_REF,
            12 to KickOffEvent.PITCH_INVASION,
        )

    override fun roll(
        die1: D6Result,
        die2: D6Result,
    ): KickOffEvent {
        val result = die1.value + die2.value
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Kick-Off Event Table.")
    }
}
