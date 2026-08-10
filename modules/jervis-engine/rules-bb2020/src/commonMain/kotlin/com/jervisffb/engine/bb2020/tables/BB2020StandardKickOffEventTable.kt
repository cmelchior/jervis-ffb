package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.common.tables.KickOffEventResult
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Kick-Off Event Table on page 41 in the rulebook.
 */
@Serializable
object BB2020StandardKickOffEventTable: KickOffTable {
    override val name: String = "Standard Kick-Off Table"
    override val entries: Map<Int, KickOffEventResult> =
        mapOf(
            2 to KickOffEventResult.GET_THE_REF,
            3 to KickOffEventResult.TIME_OUT,
            4 to KickOffEventResult.SOLID_DEFENSE,
            5 to KickOffEventResult.HIGH_KICK,
            6 to KickOffEventResult.BB2020_CHEERING_FANS,
            7 to KickOffEventResult.BRILLIANT_COACHING,
            8 to KickOffEventResult.CHANGING_WEATHER,
            9 to KickOffEventResult.QUICK_SNAP,
            10 to KickOffEventResult.BLITZ,
            11 to KickOffEventResult.OFFICIOUS_REF,
            12 to KickOffEventResult.PITCH_INVASION,
        )

    override fun roll(
        die1: D6Result,
        die2: D6Result,
    ): KickOffEventResult {
        val result = die1.value + die2.value
        return entries[result] ?: INVALID_GAME_STATE("$result was not found in the Kick-Off Event Table.")
    }
}
