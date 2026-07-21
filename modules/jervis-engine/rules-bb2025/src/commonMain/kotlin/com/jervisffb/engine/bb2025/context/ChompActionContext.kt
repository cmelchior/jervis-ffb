package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class ChompActionContext(
    val attacker: Player,
    val hasChomped: Boolean = false,
): ProcedureContext
