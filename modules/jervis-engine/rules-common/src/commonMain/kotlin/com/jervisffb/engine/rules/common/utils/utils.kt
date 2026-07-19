package com.jervisffb.engine.rules.common.utils

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import kotlin.sequences.flatMap

// Checks if an action should end immediately.
// It feels wrong to have this method here (since it contains some logic and
// reference a context). Should it be an utility method or be in the Rules
// instead?
fun Game.endActionImmediately(): Boolean {
    return (
        isTurnOver()
            || hasConceeded != null
            || getContextOrNull<ActivatePlayerContext>()?.activationEndsImmediately == true
    )
}

fun List<Skill<*>>.getRerollOptions(state: Game, type: DiceRollType, roll: DieRoll<*>, successOnFirstRoll: Boolean?): List<DiceRerollOption> {
    if (!state.rules.isRerollAllowed(listOf(roll))) return emptyList()
    return this.asSequence().filter { it is RerollSource }
        .map { it as RerollSource }
        .filter { !it.rerollUsed }
        .filter { it.canReroll(state, type, listOf(roll), successOnFirstRoll) }
        .flatMap { it: RerollSource -> it.calculateRerollOptions(type, roll, successOnFirstRoll) }
        .toList()
}

/**
 * Calculate all available re-rolls options for a Team rolling a roll type.
 * Note, if Player is the one doing the rolls, use
 * [calculateAvailableRerollsForPlayer] instead.
 *
 * If no re-rolls are available, an empty list is returned.
 *
 * This method doesn't work for BLOCK rolls.
 */
fun calculateAvailableRerollsForTeam(
    team: Team, // Team rolling the dice
    type: DiceRollType, // Which type of dice roll
    roll: List<DieRoll<*>>, // The result of the first dice
    firstRollWasSuccess: Boolean? // Whether the first roll was a success.
): List<DiceRerollOption> {
    // Team rerolls are only available to the active team
    if (team.game.activeTeam != team) return emptyList()

    val state = team.game
    val rules = team.game.rules
    val hasTeamRerolls = team.availableRerollCount > 0
    val allowedToUseTeamReroll = rules.canUseTeamReroll(state, player = null)
    val canRerollType = rules.canBeRerolledByTeamReroll(type)

    // Calculate the full list of re-roll options
    return when (canRerollType && hasTeamRerolls && allowedToUseTeamReroll) {
        true -> rules.getAvailableTeamRerolls(team).map { DiceRerollOption(it.id, roll) }
        false -> emptyList()
    }
}

/**
 * Calculate all available re-rolls options for a given roll type.
 * If no re-rolls are available, an empty list is returned.
 *
 * This method doesn't work for BLOCK rolls.
 */
fun calculateAvailableRerollsForPlayer(
    player: Player, // Player rolling the dice
    type: DiceRollType, // Which type of dice roll
    roll: DieRoll<*>, // The result of the first dice
    firstRollWasSuccess: Boolean? // Whether the first roll was a success.
): List<DiceRerollOption> {
    if (type == DiceRollType.BLOCK) throw IllegalArgumentException("Use `calculateAvailableRerollsForBlock` instead")
    val skillRerolls: List<DiceRerollOption> = player.skills.getRerollOptions(
        player.team.game,
        type,
        roll,
        firstRollWasSuccess
    )
    val teamRerolls = calculateAvailableRerollsForTeam(
        team = player.team,
        type = type,
        roll = listOf(roll),
        firstRollWasSuccess = firstRollWasSuccess
    )
    return skillRerolls + teamRerolls
}

fun calculateAvailableRerollsForBlock(
    attackingPlayer: Player,
    diceRoll: List<BlockDieRoll>
): List<DiceRerollOption> {
    // Re-rolling block dice can be pretty complex,
    // Brawler: Can reroll a single "Both Down"
    // Pro: Can reroll any single die
    // Team reroll: Can reroll all of them
    val skillRerolls = attackingPlayer.skills
        .filter { skill: Skill<*> -> skill is RerollSource }
        .map { it as RerollSource }
        .filter { it.canReroll(attackingPlayer.team.game, DiceRollType.BLOCK, diceRoll) }
        .map { DiceRerollOption(it.id, dice = null) }

    val teamRerolls = calculateAvailableRerollsForTeam(
        attackingPlayer.team,
        type = DiceRollType.BLOCK,
        diceRoll,
        null
    )
    return skillRerolls + teamRerolls
}
