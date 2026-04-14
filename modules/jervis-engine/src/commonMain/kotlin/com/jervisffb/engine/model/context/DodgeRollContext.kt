package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/**
 * Context data for a player making a dodge roll.
 *
 * @see com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
 */
data class DodgeRollContext(
    val player: Player,
    val startingSquare: PitchCoordinate,
    val targetSquare: PitchCoordinate,
    val roll: D6DieRoll? = null,
    val rollModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val useTackle: Player? = null,
    val isSuccess: Boolean = false,
): ProcedureContext {
    val modifiedResult: Int
        get() = (roll?.result?.value ?: 0) + rollModifiers.sum()
    fun copyAndAddModifier(modifier: DiceModifier): DodgeRollContext {
        return this.copy(
            rollModifiers = this.rollModifiers.add(modifier),
        )
    }
}
