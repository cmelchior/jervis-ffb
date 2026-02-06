package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.formatDiceRoll
import com.jervisffb.engine.utils.sum

data class JumpRollContext(
    val player: Player,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
): ProcedureContext {
    val result: Int
        get() = (roll?.result?.value ?: 0) + modifiers.sum()
    fun toLogString(): String = formatDiceRoll(roll!!, modifiers)
}
