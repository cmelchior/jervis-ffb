package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.modifiers.BrilliantCoachingModifiers

data class BrilliantCoachingContext(
    val kickingTeamRoll: D6Result,
    val kickingTeamModifiers: List<BrilliantCoachingModifiers> = emptyList(),
    val receivingTeamRoll: D6Result? = null,
    val receivingTeamModifiers: List<BrilliantCoachingModifiers> = emptyList(),
): ProcedureContext
