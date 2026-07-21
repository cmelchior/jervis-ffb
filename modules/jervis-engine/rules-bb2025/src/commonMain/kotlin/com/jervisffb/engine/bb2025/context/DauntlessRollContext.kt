package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class DauntlessRollContext(
    val attacker: Player,
    val defender: Player,
    val roll: D6DieRoll? = null,
    val modifier: StatModifier? = null
): ProcedureContext {
    val isSuccess = (modifier != null)
}
