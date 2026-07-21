package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate

data class DeviateRollContext(
    val from: PitchCoordinate,
    val deviateRoll: List<DieResult> = emptyList(),
    val landsAt: PitchCoordinate? = null,
    val outOfBoundsAt: PitchCoordinate? = null, // Will contain the last square before the ball went out of bounds.
): ProcedureContext
