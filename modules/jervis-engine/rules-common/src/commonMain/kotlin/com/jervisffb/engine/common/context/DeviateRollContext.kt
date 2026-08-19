package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class DeviateRollContext(
    val from: PitchCoordinate,
    val deviateRoll: List<DieResult> = emptyList(),
    val landsAt: PitchCoordinate? = null,
    val outOfBoundsAt: PitchCoordinate? = null, // Will contain the last square before the ball went out of bounds.
): ProcedureContext {
    val minD6: D6Result
        get() = deviateRoll.filterIsInstance<D6Result>().minByOrNull { it.value } ?: INVALID_GAME_STATE("No D6 result found: $deviateRoll")
}
