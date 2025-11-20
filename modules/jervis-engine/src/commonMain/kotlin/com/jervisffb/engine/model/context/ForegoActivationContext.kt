package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player

data class ForegoActivationContext(
    val player: Player,
    /**
     * If `true` the turn is ending and the engine is automatically foreging
     * activation for all remaining players.
     */
    val isEndingTurn: Boolean
): ProcedureContext
