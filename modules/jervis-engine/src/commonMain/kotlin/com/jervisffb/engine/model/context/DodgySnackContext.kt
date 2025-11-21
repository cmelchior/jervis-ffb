package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player

data class DodgySnackContext(
    val kickingTeamRoll: D6Result,
    val receivingTeamRoll: D6Result? = null,
    val kickingTeamPlayerSelected: Player? = null,
    val receivingTeamPlayerSelected: Player? = null,
    val kickingTeamSnackRoll: D6Result? = null,
    val receivingTeamSnackRoll: D6Result? = null,
): ProcedureContext
