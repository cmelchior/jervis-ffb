package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.Blitz
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.ChangingWeather
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.CheeringFans
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.GetTheRef
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.HighKick
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.TimeOut
import com.jervisffb.engine.rules.common.skills.Duration

enum class KickOffEvent(
    override val description: String,
    override val procedure: Procedure,
    override val duration: Duration
): TableResult {
    GET_THE_REF("Get the Ref", GetTheRef, Duration.END_OF_GAME),
    TIME_OUT("Time Out", TimeOut, Duration.END_OF_GAME),
    TIME_OUT_BB7("Time Out", TimeOut, Duration.END_OF_GAME),
    SOLID_DEFENSE("Solid Defense", SolidDefense, Duration.END_OF_GAME),
    SOLID_DEFENSE_BB7("Solid Defense", TimeOut, Duration.END_OF_GAME),
    HIGH_KICK("High Kick", HighKick, Duration.END_OF_GAME),
    CHEERING_FANS("Cheering Fans", CheeringFans, Duration.END_OF_GAME),
    BRILLIANT_COACHING("Brilliant Coaching", BrilliantCoaching, Duration.END_OF_GAME),
    CHANGING_WEATHER("Changing Weather", ChangingWeather, Duration.END_OF_GAME),
    QUICK_SNAP("Quick Snap", QuickSnap, Duration.END_OF_GAME),
    QUICK_SNAP_BB7("Quick Snap", QuickSnap, Duration.END_OF_GAME),
    BLITZ("Blitz", Blitz, Duration.END_OF_GAME),
    BLITZ_BB7("Blitz", Blitz, Duration.END_OF_GAME),
    OFFICIOUS_REF("Officious Ref", OfficiousRef, Duration.END_OF_GAME),
    PITCH_INVASION("Pitch Invasion", PitchInvasion, Duration.END_OF_GAME),
}
