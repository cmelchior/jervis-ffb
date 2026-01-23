package com.jervisffb.ui.game.state

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.common.procedures.actions.block.standard.calculateBlockDiceToRoll
import com.jervisffb.engine.rules.common.skills.SkillType

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

    if (attacker.hasSkill(SkillType.HORNS) && isBlitzing) {
        attackerStrength += 1
    }

    val offensiveAssists = rules.calculateOffensiveAssists(attacker, defender)
    val defensiveAssists = rules.calculateDefensiveAssists(defender, attacker)

    return calculateBlockDiceToRoll(attackerStrength, offensiveAssists, defenderStrength, defensiveAssists)
}
