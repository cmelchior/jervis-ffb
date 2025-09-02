package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

/**
 * Context data for a player making a dodge rol.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.move.DodgeRoll]
 */
data class DodgeRollContext(
    val player: Player,
    val startingSquare: FieldCoordinate,
    val targetSquare: FieldCoordinate,
    val roll: D6DieRoll? = null,
    val rollModifiers: List<DiceModifier> = emptyList(),
    val isSuccess: Boolean = true,
): ProcedureContext {
    fun copyAndAddModifier(twoHeads: DiceModifier): DodgeRollContext {
        return this.copy(
            rollModifiers = this.rollModifiers + twoHeads,
        )
    }
}
