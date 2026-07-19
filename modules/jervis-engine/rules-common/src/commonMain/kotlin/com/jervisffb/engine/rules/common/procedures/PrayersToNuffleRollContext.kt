package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent

/**
 * Context data required to track rolling on the Prayers of Nuffle.
 */
data class PrayersToNuffleRollContext(
    val team: Team,
    val rollsRemaining: Int,
    val result: PrayerToNuffleEvent? = null,
    val resultApplied: Boolean = false
) : ProcedureContext
