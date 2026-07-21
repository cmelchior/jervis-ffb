package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class AnimalSavageryContext(
    val player: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
    val selectedAdjacentPlayer: Player? = null,
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}
