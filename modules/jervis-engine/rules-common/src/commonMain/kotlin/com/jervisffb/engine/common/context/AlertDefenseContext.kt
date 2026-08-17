package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate

data class AlertDefenseContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
    val target: PitchCoordinate? = null,
): ProcedureContext {
    val playersLeft = (roll.value + 3) - playersMoved.size
}
