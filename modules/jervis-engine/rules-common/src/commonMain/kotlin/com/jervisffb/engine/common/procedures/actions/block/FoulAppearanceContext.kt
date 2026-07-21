package com.jervisffb.engine.common.procedures.actions.block

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class FoulAppearanceContext(
    val attacker: Player,
    val defender: Player,
    val roll: D6DieRoll? = null,
    // `true` means that the `defender` avoided being affected by Foul Appearance
    val isSuccess: Boolean = false,
): ProcedureContext
