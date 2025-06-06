package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll

data class PassingInterferenceRollContext(
    val player: Player,
    val target: Int,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}
