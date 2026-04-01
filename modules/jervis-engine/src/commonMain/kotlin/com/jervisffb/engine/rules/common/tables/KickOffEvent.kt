package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.Charge
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BB2020CheeringFans
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BB2025CheeringFans
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.Blitz
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.ChangingWeather
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.GetTheRef
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.HighKick
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.TimeOut
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * List all possible kick-off event results across all rulesets.
 */
enum class KickOffEvent(
    override val description: String,
    override val procedure: Procedure,
    override val duration: Duration
): TableResult {
    BLITZ("Blitz", Blitz, Duration.IMMEDIATE),
    BLITZ_BB7("Blitz", Blitz, Duration.IMMEDIATE),
    BRILLIANT_COACHING("Brilliant Coaching", BrilliantCoaching, Duration.IMMEDIATE),
    CHANGING_WEATHER("Changing Weather", ChangingWeather, Duration.IMMEDIATE),
    CHARGE("Charge!", Charge, Duration.IMMEDIATE),
    BB2020_CHEERING_FANS("Cheering Fans", BB2020CheeringFans, Duration.IMMEDIATE),
    BB2025_CHEERING_FANS("Cheering Fans", BB2025CheeringFans, Duration.IMMEDIATE),
    DODGY_SNACK("Dodgy Snack", DodgySnack, Duration.IMMEDIATE),
    GET_THE_REF("Get the Ref", GetTheRef, Duration.IMMEDIATE),
    HIGH_KICK("High Kick", HighKick, Duration.IMMEDIATE),
    OFFICIOUS_REF("Officious Ref", OfficiousRef, Duration.IMMEDIATE),
    PITCH_INVASION("Pitch Invasion", PitchInvasion, Duration.IMMEDIATE),
    QUICK_SNAP("Quick Snap", QuickSnap, Duration.IMMEDIATE),
    QUICK_SNAP_BB7("Quick Snap", QuickSnap, Duration.IMMEDIATE),
    SOLID_DEFENSE("Solid Defense", SolidDefense, Duration.IMMEDIATE),
    SOLID_DEFENSE_BB7("Solid Defense", TimeOut, Duration.IMMEDIATE),
    TIME_OUT("Time Out", TimeOut, Duration.IMMEDIATE),
    TIME_OUT_BB7("Time Out", TimeOut, Duration.IMMEDIATE),
}
