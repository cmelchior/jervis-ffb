package com.jervisffb.ui.state

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.bb2020.skills.Horns

/**
 * Calculate the expected number of dice to be rolled if [attacker] blocks [defender].
 * The result assumes both sides use the max amount of assists and use all relevant skills.
 *
 * Positive numbers indicate attacker chooses, negative that defender chooses.
 */
fun calculateAssumedNoOfBlockDice(state: Game, attacker: Player, defender: Player, isBlitzing: Boolean = false): Int {
    val rules = state.rules
    var attackerStrength = attacker.strength
    var defenderStrength = defender.strength

    // TODO Horns, Dauntless, Multiple Block. Are other things affecting strength?
    if (attacker.hasSkill<Horns>() && isBlitzing) {
        attackerStrength += 1
    }

    val offensiveAssists = defender.coordinates.getSurroundingCoordinates(state.rules)
        .mapNotNull { state.field[it].player }
        .count { player -> rules.canOfferAssistAgainst(player, defender) }

    val defensiveAssists =
        attacker.coordinates.getSurroundingCoordinates(rules)
            .mapNotNull { state.field[it].player }
            .count { player -> rules.canOfferAssistAgainst(player, attacker) }

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
