package com.jervisffb.engine.rules.bb2020

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.utils.sum

/**
 * Test against agility as described on page 29 in the rulebook.
 *
 * @return `true` if the test succeeds or `false` if not.
 */
fun testAgainstAgility(player: Player, d6: D6Result, modifiers: List<DiceModifier>): Boolean {
    val target = player.agility
    return when (d6.value) {
        1 -> false
        in 2..5 -> d6.value + modifiers.sum() >= target
        6 -> true
        else -> error("Invalid value: ${d6.value}")
    }
}
