package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class MascotRollContext(
    val team: Team,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext
