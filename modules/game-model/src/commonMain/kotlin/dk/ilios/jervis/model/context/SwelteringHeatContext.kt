package dk.ilios.jervis.model.context

import dk.ilios.jervis.actions.D3Result

/**
 * Context data for handling rolling for Sweltering Heat.
 *
 * @see [dk.ilios.jervis.procedures.weather.SwelteringHeat]
 */
data class SwelteringHeatContext(
    val homeRoll: D3Result? = null,
    val awayRoll: D3Result? = null,
): ProcedureContext
