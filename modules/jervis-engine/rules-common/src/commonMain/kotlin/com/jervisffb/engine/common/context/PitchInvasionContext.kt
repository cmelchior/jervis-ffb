package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.context.ProcedureContext

data class PitchInvasionContext(
    val kickingRoll: D6Result,
    val kickingResult: Int = 0,
    val kickingPlayersAffected: Int = 0,
    val receivingRoll: D6Result? = null,
    val receivingResult: Int = 0,
    val receivingPlayersAffected: Int = 0

): ProcedureContext
