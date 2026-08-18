package com.jervisffb.engine.common.tables

import com.jervisffb.engine.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.common.procedures.tables.kickoff.ChangingWeather
import com.jervisffb.engine.common.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.common.procedures.tables.kickoff.GetTheRef
import com.jervisffb.engine.common.procedures.tables.kickoff.HighKick
import com.jervisffb.engine.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.common.procedures.tables.kickoff.TimeOut
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.KickOffEvent

/**
 * Enumerates all possible kick-off event results across BB2020 and BB2025.
 *
 * TODO Figure out a way to split this into BB2020 and BB2025 specific classes.
 */
enum class CommonKickOffEventResult(
    override val description: String,
    override val procedure: Procedure = DummyProcedure,
    override val duration: Duration
): KickOffEvent {
    BRILLIANT_COACHING("Brilliant Coaching", BrilliantCoaching, Duration.IMMEDIATE),
    CHANGING_WEATHER("Changing Weather", ChangingWeather, Duration.IMMEDIATE),
    DODGY_SNACK("Dodgy Snack", DodgySnack, Duration.IMMEDIATE),
    GET_THE_REF("Get the Ref", GetTheRef, Duration.IMMEDIATE),
    HIGH_KICK("High Kick", HighKick, Duration.IMMEDIATE),
    OFFICIOUS_REF("Officious Ref", OfficiousRef, Duration.IMMEDIATE),
    PITCH_INVASION("Pitch Invasion", PitchInvasion, Duration.IMMEDIATE),
    QUICK_SNAP("Quick Snap", QuickSnap, Duration.IMMEDIATE),
    SOLID_DEFENSE("Solid Defense", SolidDefense, Duration.IMMEDIATE),
    TIME_OUT("Time Out", TimeOut, Duration.IMMEDIATE),
}
