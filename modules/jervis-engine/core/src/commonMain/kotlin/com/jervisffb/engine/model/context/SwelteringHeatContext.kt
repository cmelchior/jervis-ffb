package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D3Result

/**
 * Context data for handling rolling for Sweltering Heat.
 *
 * @see [com.jervisffb.engine.rules.common.procedures.tables.weather.SwelteringHeat]
 */
data class SwelteringHeatContext(
    val homeRoll: D3Result? = null,
    val awayRoll: D3Result? = null,
): ProcedureContext
