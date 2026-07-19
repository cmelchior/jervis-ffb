package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

/**
 * Context data for rolling for Tentacles. Consumed by each ruleset's
 * `TentaclesStep` procedure.
 */
data class TentaclesRollContext(
    val movingPlayer: Player,
    val tentaclePlayer: Player? = null,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
) : ProcedureContext {
    // The sum of modifiers, currently not used for anything
    fun diceModifier(): Int = modifiers.fold(0) { acc: Int, el: DiceModifier -> acc + el.modifier }
    val rerolled: Boolean
        get() = roll!!.rerollSource != null && roll.rerolledResult != null
}
