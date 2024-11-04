package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

/**
 * Returns the number of block dice to roll during a block. x < 0 means the defender
 * chooses, x > 0 means the attacker chooses
 */
fun calculateBlockDiceToRoll(
    attackerStrength: Int,
    offensiveAssists: Int,
    defenderStrength: Int,
    defensiveAssists: Int
): Int {
    val attackerTotal = attackerStrength + offensiveAssists
    val defenderTotal = defenderStrength + defensiveAssists
    return when {
        attackerTotal == defenderTotal -> 1
        attackerTotal > (defenderTotal * 2) -> 3
        defenderTotal > (attackerTotal * 2) -> -3
        attackerTotal > defenderTotal -> 2
        defenderTotal > attackerTotal -> -2
        else -> 0 // Unclear, do not report anything
    }
}

