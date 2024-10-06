package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D3Result

/**
 * Context data when rolling for Bad Habits found on the Prayers of Nuffle
 * table.
 */
data class BadHabitsContext(
    val roll: D3Result,
    val mustSelectPlayers: Int
): ProcedureContext

