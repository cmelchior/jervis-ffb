package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class DealWithSecretWeaponsContext(
    val selectedPlayer: Player? = null,
): ProcedureContext
