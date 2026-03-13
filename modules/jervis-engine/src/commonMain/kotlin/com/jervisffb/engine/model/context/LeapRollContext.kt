package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.formatDiceRoll
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class LeapRollContext(
    val player: Player,
    val startingSquare: FieldCoordinate,
    val modifiers: PersistentList<DiceModifier> = persistentListOf(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
): ProcedureContext {
    val modifiedResult: Int
        get() = (roll?.result?.value ?: 0) + modifiers.sum()
    fun toLogString(): String = formatDiceRoll(roll!!, modifiers)
}
