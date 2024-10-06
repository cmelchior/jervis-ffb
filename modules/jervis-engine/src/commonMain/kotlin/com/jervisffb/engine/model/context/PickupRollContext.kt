package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.utils.sum

/**
 * Context data for picking up the ball.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.PickupRoll]
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
