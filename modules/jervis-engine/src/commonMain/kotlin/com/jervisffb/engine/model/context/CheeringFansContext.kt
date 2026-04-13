package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.modifiers.DiceModifier

data class CheeringFansContext(
    val kickingTeamRoll: D6Result? = null,
    val receivingTeamRoll: D6Result? = null,
    val kickingTeamModifiers: List<DiceModifier> = emptyList(),
    val receivingTeamModifiers: List<DiceModifier> = emptyList(),
    val winner: Team? = null, // If `null`, both teams rolled the same
): ProcedureContext
