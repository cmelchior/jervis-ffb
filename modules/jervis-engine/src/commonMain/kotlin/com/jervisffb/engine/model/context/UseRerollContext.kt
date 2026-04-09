package com.jervisffb.engine.model.context

import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.RerollSource

/**
 * Wrap the choice of the reroll type used and whether it can be used to
 * reroll the current dice.
 *
 * Some reroll types, like Pro, count as being used but might fail, so they
 * mark the dice as being re-rolled, without actually doing it.
 */
data class UseRerollContext(
    // Type of Dice Roll. Used to do an easy first-pass filter for reroll options.
    val roll: DiceRollType,
    // Reference to Skill or Team Reroll variant (or other effect) that can
    // supply a reroll option.
    val source: RerollSource,
//    // If `null` after `UseRerollSource` has been run, it means that no rerolls
//    // are allowed after all. E.g. failing a Mascot or Pro roll.
    val selectedRerollOption: DiceRerollOption? = null
) : ProcedureContext {

    val rerollAllowed: Boolean = (selectedRerollOption != null)

    fun getRerollOption(value: DieRoll<*>, wasSuccess: Boolean?): List<DiceRerollOption> {
        return source.calculateRerollOptions(roll, value, wasSuccess)
    }
}
