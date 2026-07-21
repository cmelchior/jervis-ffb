package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class ChainsawActionContext(
    val attacker: Player,
    val hasUsedChainsaw: Boolean = false,
): ProcedureContext
