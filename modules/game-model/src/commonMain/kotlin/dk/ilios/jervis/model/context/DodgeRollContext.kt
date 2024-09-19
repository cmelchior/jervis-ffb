package dk.ilios.jervis.model.context

import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.D6DieRoll

/**
 * Context data for a player making a dodge rol.
 *
 * @see [dk.ilios.jervis.procedures.actions.move.DodgeRoll]
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
