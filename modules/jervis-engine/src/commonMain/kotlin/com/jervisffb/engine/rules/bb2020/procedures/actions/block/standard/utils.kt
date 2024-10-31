package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext

// Helper method to share logic between roll and reroll
fun calculateNoOfBlockDice(state: Game, isBlitzing: Boolean = false): Int {
    val context = state.getContext<BlockContext>()
    val attackStrength = context.attacker.strength + context.offensiveAssists
    val defenderStrength = context.defender.strength + context.defensiveAssists
    return when {
        attackStrength == defenderStrength -> 1
        attackStrength > defenderStrength * 2 -> 3
        defenderStrength > attackStrength * 2 -> 3
        else -> 2
    }
}
