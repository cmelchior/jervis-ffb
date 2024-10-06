package com.jervisffb.engine.model.context

import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.skills.RerollSource

/**
 * Wrap the choice of the reroll type used, and whether it can be used to
 * reroll the current dice.
 *
 * Some reroll types like Pro, count as being used, but might fail, so they
 * do not allow you to reroll the dice roll.
 */
data class UseRerollContext(
    val roll: DiceRollType,
    val source: RerollSource,
    val rerollAllowed: Boolean = true
) : ProcedureContext
