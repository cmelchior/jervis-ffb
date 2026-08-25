package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.bb2020.procedures.table.kickoff.BB2020CheeringFans
import com.jervisffb.engine.common.procedures.tables.kickoff.Blitz
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.KickOffEvent

/**
 * Enumerates all possible kick-off in BB2020.
 */
enum class BB2020KickOffEventResult(
    override val description: String,
    override val procedure: Procedure = DummyProcedure,
    override val duration: Duration
): KickOffEvent {
    DODGY_SNACK("Dodgy Snack", DummyProcedure, duration = Duration.IMMEDIATE),
    BLITZ("Blitz", Blitz, Duration.IMMEDIATE),
    CHEERING_FANS(description = "Cheering Fans", BB2020CheeringFans, duration = Duration.IMMEDIATE)
}
