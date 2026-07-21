package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class BothDownContext(
    val attacker: Player,
    val defender: Player,
    val attackUsesBlock: Boolean = false,
    val defenderUsesBlock: Boolean = false,
    val attackerUsesWrestle: Boolean = false,
    val defenderUsesWrestle: Boolean = false,
) : ProcedureContext
