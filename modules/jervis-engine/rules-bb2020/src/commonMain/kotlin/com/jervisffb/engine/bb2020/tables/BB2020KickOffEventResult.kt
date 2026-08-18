package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.common.procedures.tables.kickoff.Blitz
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.Rules
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
    BLITZ("Blitz", Blitz, Duration.IMMEDIATE),
    CHEERING_FANS(description = "Cheering Fans", duration = Duration.IMMEDIATE) {
        override fun resolveProcedure(rules: Rules): Procedure = rules.cheeringFansStep
    },
}
