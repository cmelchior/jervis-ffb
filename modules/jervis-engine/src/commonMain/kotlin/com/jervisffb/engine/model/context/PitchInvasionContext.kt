package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result

data class PitchInvasionContext(
    val kickingRoll: D6Result,
    val kickingResult: Int = 0,
    val kickingPlayersAffected: Int = 0,
    val receivingRoll: D6Result? = null,
    val receivingResult: Int = 0,
    val receivingPlayersAffected: Int = 0

): ProcedureContext
