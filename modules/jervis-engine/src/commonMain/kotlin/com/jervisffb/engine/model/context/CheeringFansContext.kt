package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Team

data class CheeringFansContext(
    val kickingTeamRoll: D6Result,
    val receivingTeamRoll: D6Result? = null,
    val winner: Team? = null, // If `null`, both teams rolled the same
): ProcedureContext
