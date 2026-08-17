package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.bb2025.procedures.table.kickoff.AlertDefense
import com.jervisffb.engine.bb2025.procedures.table.kickoff.Charge
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.KickOffEvent
import com.jervisffb.engine.common.tables.CommonKickOffEventResult

/**
 * Lists BB2025 specific kickoff event results.
 *
 * See [CommonKickOffEventResult] for results shared with BB2020.
 */
enum class BB2025KickOffEventResult(
    override val description: String,
    override val procedure: Procedure = DummyProcedure,
    override val duration: Duration
): KickOffEvent {
    ALERT_DEFENSE("Alert Defense", AlertDefense, Duration.IMMEDIATE),
    CHARGE("Charge!", Charge, Duration.IMMEDIATE),
    CHEERING_FANS(description = "Cheering Fans", duration = Duration.IMMEDIATE) {
        override fun resolveProcedure(rules: Rules): Procedure = rules.cheeringFansStep
    },
}
