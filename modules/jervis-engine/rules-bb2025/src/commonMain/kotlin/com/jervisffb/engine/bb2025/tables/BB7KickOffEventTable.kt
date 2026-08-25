package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.common.tables.KickOffEventResultCommon
import com.jervisffb.engine.rules.common.tables.KickOffEvent
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Kick-Off Event Table on page XXX in Spike 22.
 */
@Serializable
object BB7KickOffEventTable: KickOffTable {
    override val name: String = "Sevens Kick-Off Table"
    override val entries: Map<Int, KickOffEvent> =
        mapOf(
            2 to KickOffEventResultCommon.GET_THE_REF,
            3 to KickOffEventResultCommon.TIME_OUT,
            4 to KickOffEventResult2025.ALERT_DEFENSE,
            5 to KickOffEventResultCommon.HIGH_KICK,
            6 to KickOffEventResult2025.CHEERING_FANS,
            7 to KickOffEventResultCommon.BRILLIANT_COACHING,
            8 to KickOffEventResultCommon.CHANGING_WEATHER,
            9 to KickOffEventResultCommon.QUICK_SNAP,
            10 to KickOffEventResult2025.CHARGE,
            11 to KickOffEventResult2025.DODGY_SNACK,
            12 to KickOffEventResultCommon.PITCH_INVASION,
        )

    override fun roll(
        die1: D6Result,
        die2: D6Result,
    ): KickOffEvent {
        val result = die1.value + die2.value
        return entries[result] ?: INVALID_GAME_STATE("$result was not found in the Kick-Off Event Table.")
    }
}
