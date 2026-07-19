package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player

data class ScoringATouchDownContext(
    val player: Player,
    val isTouchdownScored: Boolean = false
): ProcedureContext
