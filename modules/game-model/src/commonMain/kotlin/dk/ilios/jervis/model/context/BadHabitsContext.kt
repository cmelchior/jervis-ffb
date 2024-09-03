package dk.ilios.jervis.model.context

import dk.ilios.jervis.actions.D3Result

/**
 * Context data when rolling for Bad Habits found on the Prayers of Nuffle
 * table.
 */
data class BadHabitsContext(
    val roll: D3Result,
    val mustSelectPlayers: Int
): ProcedureContext

