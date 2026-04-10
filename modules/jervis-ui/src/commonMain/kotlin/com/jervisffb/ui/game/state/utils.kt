package com.jervisffb.ui.game.state

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.rules.common.procedures.actions.block.calculateBlockDiceToRoll
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Calculate the expected number of dice to be rolled if [attacker] blocks [defender].
 * The result assumes both sides use the max number of assists and use all relevant skills.
 *
 * Positive numbers indicate the attacker chooses.
 * Negative, the defender chooses.
 */
fun calculateAssumedNoOfBlockDice(state: Game, attacker: Player, defender: Player, isBlitzing: Boolean = false): Int {
    val rules = state.rules
    var attackerStrength = attacker.strength
    var defenderStrength = defender.strength

    // For now, we ignore modifiers like Dauntless and Multiple Block as they are hard to account for at this stage. We
    // only take into account skills that 100% will work, like Horns.
    if (attacker.hasSkill(SkillType.HORNS) && isBlitzing) {
        attackerStrength += 1
    }

    // In BB2025, Cheering Fans have a special offensive assist for the first Block.
    val cheeringFansExtraAssist = when (attacker.team.hasFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST)) {
        true -> 1
        false -> 0
    }

    // Calculate assists
    val offensiveAssists = rules.calculateOffensiveAssists(attacker, defender) + cheeringFansExtraAssist
    val defensiveAssists = rules.calculateDefensiveAssists(defender, attacker)

    return calculateBlockDiceToRoll(attackerStrength, offensiveAssists, defenderStrength, defensiveAssists)
}
