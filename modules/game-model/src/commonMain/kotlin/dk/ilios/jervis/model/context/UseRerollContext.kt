package dk.ilios.jervis.model.context

import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource

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
