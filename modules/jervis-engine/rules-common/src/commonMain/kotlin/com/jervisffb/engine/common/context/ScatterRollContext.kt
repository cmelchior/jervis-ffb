package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate

data class ScatterRollContext(
    val from: PitchCoordinate,
    val scatterRoll: List<D8Result> = emptyList(),
    val landsAt: PitchCoordinate? = null,
    val outOfBoundsAt: PitchCoordinate? = null, // Will contain the last square before the ball went out of bounds.
): ProcedureContext
