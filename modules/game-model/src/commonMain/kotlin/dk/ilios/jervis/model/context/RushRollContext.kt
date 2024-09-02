package dk.ilios.jervis.model.context

import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.D6DieRoll

/**
 * Context data for rushing a player.
 *
 * @see [dk.ilios.jervis.procedures.actions.move.StandardMoveStep]
 * @see [dk.ilios.jervis.procedures.actions.move.RushRoll]
 */
data class RushRollContext(
    val player: Player,
    val target: FieldCoordinate,
    val roll: D6DieRoll? = null,
    val modifiers: List<DiceModifier> = emptyList(),
    val isSuccess: Boolean = false
): ProcedureContext {
    fun copyAndAddModifier(modifier: DiceModifier): RushRollContext {
        return this.copy(
            modifiers = this.modifiers + modifier,
        )
    }
}
