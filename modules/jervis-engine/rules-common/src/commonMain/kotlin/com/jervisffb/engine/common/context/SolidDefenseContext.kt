package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class SolidDefenseContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3/1
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
): ProcedureContext
