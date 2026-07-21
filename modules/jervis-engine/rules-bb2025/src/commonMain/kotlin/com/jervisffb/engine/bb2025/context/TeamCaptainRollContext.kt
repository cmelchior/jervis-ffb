package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class TeamCaptainRollContext(
    val player: Player, // Player with Team Captain
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext
