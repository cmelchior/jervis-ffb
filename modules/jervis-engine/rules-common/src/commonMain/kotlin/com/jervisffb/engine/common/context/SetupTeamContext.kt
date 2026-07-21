package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext

data class SetupTeamContext(
    val team: Team,
    var currentPlayer: Player? = null
): ProcedureContext
