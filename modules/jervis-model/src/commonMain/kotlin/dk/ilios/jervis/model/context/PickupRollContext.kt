package dk.ilios.jervis.model.context

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.utils.sum

/**
 * Context data for picking up the ball.
 *
 * @see [dk.ilios.jervis.procedures.PickupRoll]
 */
data class PickupRollContext(
    val player: Player,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
    ) : ProcedureContext {
    // The sum of modifiers
    fun diceModifier(): Int = modifiers.fold(0) { acc: Int, el: DiceModifier -> acc + el.modifier }
    val rerolled: Boolean
        get() = roll!!.rerollSource != null && roll.rerolledResult != null
    val target
        get() = player.agility + modifiers.sum()
}
