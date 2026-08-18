package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.common.tables.CommonKickOffEventResult
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
            2 to CommonKickOffEventResult.GET_THE_REF,
            3 to CommonKickOffEventResult.TIME_OUT,
            4 to BB2025KickOffEventResult.ALERT_DEFENSE,
            5 to CommonKickOffEventResult.HIGH_KICK,
            6 to BB2025KickOffEventResult.CHEERING_FANS,
            7 to CommonKickOffEventResult.BRILLIANT_COACHING,
            8 to CommonKickOffEventResult.CHANGING_WEATHER,
            9 to CommonKickOffEventResult.QUICK_SNAP,
            10 to BB2025KickOffEventResult.CHARGE,
            11 to CommonKickOffEventResult.OFFICIOUS_REF,
            12 to CommonKickOffEventResult.PITCH_INVASION,
        )

    override fun roll(
        die1: D6Result,
        die2: D6Result,
    ): KickOffEvent {
        val result = die1.value + die2.value
        return entries[result] ?: INVALID_GAME_STATE("$result was not found in the Kick-Off Event Table.")
    }
}
