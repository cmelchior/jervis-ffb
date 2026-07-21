package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class ChompContext(
    val attacker: Player,
    val defender: Player? = null,
    val chompRoll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext
