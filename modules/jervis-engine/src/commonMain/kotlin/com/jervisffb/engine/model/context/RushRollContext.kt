package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll

/**
 * Context data for rushing a player.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.move.StandardMoveStep]
 * @see [com.jervisffb.rules.bb2020.procedures.actions.move.RushRoll]
 */
data class RushRollContext(
    val player: Player,
    val target: FieldCoordinate,
    val roll: D6DieRoll? = null,
    val modifiers: List<DiceModifier> = emptyList(),
    val isSuccess: Boolean = false
): ProcedureContext {
    fun copyAndAddModifier(vararg modifiers: DiceModifier): RushRollContext {
        return this.copy(
            modifiers = this.modifiers + modifiers,
        )
    }
}
