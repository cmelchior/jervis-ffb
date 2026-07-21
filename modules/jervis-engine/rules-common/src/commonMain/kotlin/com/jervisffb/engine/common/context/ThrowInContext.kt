package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate

data class ThrowInContext(
    val ball: Ball,
    val outOfBoundsAt: PitchCoordinate,
    val directionRoll: D3Result? = null,
    val direction: Direction? = null,
    val distance: List<D6Result> = emptyList(),
): ProcedureContext
