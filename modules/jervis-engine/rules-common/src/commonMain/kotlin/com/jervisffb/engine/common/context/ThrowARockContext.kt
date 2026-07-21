package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class ThrowARockContext(
    val stallingPlayers: List<Player>,
    val currentPlayer: Player? = null
): ProcedureContext
