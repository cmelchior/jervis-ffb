package dk.ilios.jervis.model.context

import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.procedures.D6DieRoll

data class CatchRollContext(
    val catchingPlayer: Player,
    val target: Int,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
) : ProcedureContext {
    fun diceModifier(): Int = modifiers.sumOf { it.modifier }
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}
