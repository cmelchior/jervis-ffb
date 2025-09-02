package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class CatchRollContext(
    val catchingPlayer: Player,
    val target: Int,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}
