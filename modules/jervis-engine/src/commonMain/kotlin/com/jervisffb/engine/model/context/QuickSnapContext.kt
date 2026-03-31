package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

data class QuickSnapContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
    val target: FieldCoordinate? = null,
): ProcedureContext {
    val playersLeft = (roll.value + 3) - playersMoved.size
}
