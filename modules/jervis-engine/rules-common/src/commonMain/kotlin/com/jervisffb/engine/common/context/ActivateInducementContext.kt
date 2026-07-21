package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.inducements.Timing

data class ActivateInducementContext(
    val team: Team,
    val timing: Timing
): ProcedureContext
