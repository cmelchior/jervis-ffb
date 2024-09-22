package dk.ilios.jervis.procedures.actions.block.standard

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.block.BlockContext

// Helper method to share logic between roll and reroll
fun calculateNoOfBlockDice(state: Game): Int {
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
