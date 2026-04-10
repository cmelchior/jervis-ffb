package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemovePlayerSkill
import com.jervisffb.engine.commands.RemovePlayerStatModifier
import com.jervisffb.engine.commands.RemovePlayerStatusEffect
import com.jervisffb.engine.commands.RemovePrayersToNuffle
import com.jervisffb.engine.commands.RemoveTeamReroll
import com.jervisffb.engine.commands.RemoveTeamStatusEffect
import com.jervisffb.engine.commands.ResetShadowingSkill
import com.jervisffb.engine.commands.SetPlayerAvailability
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.SetSpecialPlayCardActive
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.InfamousCoachAbility
import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.SpecialPlayCard
import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.JUMP_DISTANCE
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
import com.jervisffb.engine.rules.common.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Returns a list of all possible move actions for a given player.
 * This should take into account normal moves, rushing, jump and all
 * skills like Leap and Ball & Chain
 *
 * TODO Maybe not ball an chain? :thinking:
 */
// TODO
fun calculateMoveTypesAvailable(state: Game, player: Player): SelectMoveType? {
    if (state.endActionImmediately()) {
        return null
    }

    val rules = state.rules
    val options = mutableListOf<MoveType>()

    // Standup
    if (player.location.isOnField(rules) && player.state == PlayerState.PRONE) {
        options.add(MoveType.STAND_UP)
    }

    // If Player is Rooted, they cannot leave their current square, so exit early
    if (player.hasStatusEffect(PlayerStatusEffectType.ROOTED)) {
        return when (options.isNotEmpty()){
            true -> SelectMoveType(options)
            false -> null
        }
    }

    // Normal move (with a potential rush)
    // Sprint is still optional, but here we assume it will be used if needed
    val extraSprintRush = if (player.isSkillAvailable(SkillType.SPRINT)) SPRINT_EXTRA_RUSHES else 0
    if (player.movesLeft + player.rushesLeft + extraSprintRush >= 1 && rules.isStanding(player)) {
        options.add(MoveType.STANDARD)
    }

    // Jump, if next to a prone player and space on the opposite side
    val hasMoveLeft = player.movesLeft + player.rushesLeft + extraSprintRush >= JUMP_DISTANCE && rules.isStanding(player)
    val legalJumpSquares = player.coordinates.getSurroundingCoordinates(rules, distance = 1)
        .mapNotNull { state.field[it].player }
        .filter { !rules.isStanding(it) }
        .any {
            // A jumping player can only jump to the same squares you would normally push the player
            // to. See page 45 in the rulebook.
            // This should be kept up to date with `JumpStep`
            rules.getPushOptions(player, it).any { coords ->
                coords.isOnField(rules) && state.field[coords].isUnoccupied()
            }
        }

    if (hasMoveLeft && legalJumpSquares) {
        options.add(MoveType.JUMP)
    }

    // Leap and Pogo
    val allSquares = player.coordinates.getSurroundingCoordinates(rules, distance = JUMP_DISTANCE)
    val adjacentSquares = player.coordinates.getSurroundingCoordinates(rules, distance = 1)
    val legalLeapSquares = (allSquares - adjacentSquares.toSet()).any { state.field[it].isUnoccupied() }
    if (hasMoveLeft && legalLeapSquares && player.isSkillAvailable(SkillType.LEAP)) {
        options.add(MoveType.LEAP)
    }
    if (hasMoveLeft && legalLeapSquares && player.isSkillAvailable(SkillType.POGO_STICK)) {
        options.add(MoveType.POGO)
    }

    // Skills
    // Ball & Chain
    // Others?

    return if (options.isNotEmpty()) SelectMoveType(options) else null
}

/**
 * Returns all the reachable squares a player can go to using a specific type of
 * move.
 */
fun calculateOptionsForMoveType(state: Game, rules: Rules, player: Player, type: MoveType): List<SelectFieldLocation> {
    // How many squares of movement are used to Jump
    return when (type) {
        MoveType.JUMP -> {
            val hasMoveLeft = player.movesLeft + player.rushesLeft >= JUMP_DISTANCE && rules.isStanding(player)
            val needRush = player.movesLeft < JUMP_DISTANCE
            if (hasMoveLeft) {
                val eligibleTargetSquares = player.coordinates.getSurroundingCoordinates(rules, distance = 1)
                    .mapNotNull { state.field[it].player }
                    .filter { !rules.isStanding(it) }
                    .flatMap {
                        rules.getPushOptions(player, it)
                            .filter { coords ->
                                // A jumping player can only jump to the same squares you would normally push the to.
                                // See page 45 in the BB2020 rulebook.
                                // See page 56 in the BB2025 rulebook.
                                // This should be kept up to date with `calculateMoveTypesAvailable()`
                                coords.isOnField(rules) && state.field[coords].isUnoccupied()
                            }
                    }
                    .map { TargetSquare.jump(it, needRush) }
                    .let { SelectFieldLocation(it) }
                listOf(eligibleTargetSquares)
            } else {
                emptyList()
            }
        }
        MoveType.LEAP,
        MoveType.POGO -> {
            val hasMoveLeft = player.movesLeft + player.rushesLeft >= JUMP_DISTANCE && rules.isStanding(player)
            val needRush = player.movesLeft < JUMP_DISTANCE
            if (hasMoveLeft) {
                val allSquares = player.coordinates.getSurroundingCoordinates(rules, distance = JUMP_DISTANCE)
                val adjacentSquares = player.coordinates.getSurroundingCoordinates(rules, distance = 1)
                val eligibleTargetSquares = (allSquares - adjacentSquares.toSet())
                    .filter { state.field[it].isUnoccupied() }
                    .map {
                        when (type) {
                            MoveType.LEAP -> TargetSquare.leap(it, needRush)
                            MoveType.POGO -> TargetSquare.pogo(it, needRush)
                        }
                    }
                    .let { SelectFieldLocation(it) }
                listOf(eligibleTargetSquares)
            } else {
                emptyList()
            }
        }
        MoveType.STANDARD -> {
            val requiresDodge = rules.calculateMarks(state, player.team, player.coordinates) > 0
            if (player.movesLeft + player.rushesLeft > 0) {
                player.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isUnoccupied() }
                    .map {
                        TargetSquare.move(it, player.movesLeft <= 0, requiresDodge)
                    }
                    .let { targets ->
                        if (targets.isEmpty()) {
                            emptyList()
                        } else {
                            listOf(SelectFieldLocation(targets))
                        }
                    }
            } else {
                emptyList()
            }
        }
        MoveType.STAND_UP -> TODO()
    }
}

/**
 * Returns the [Command] for setting the available number of rushes for the current action.
 *
 * Currently, this is called at the beginning of all actions with move components. This is
 * probably wrong, and it should be called when a player runs out of normal moves.
 */
fun getSetPlayerRushesCommand(rules: Rules, player: Player): Command {
    // Sprint will be applied later (if neeed), so here we just add the base rushes
    // available.
    val rushesPrAction = rules.rushesPrAction
    return SetPlayerRushesLeft(player, rushesPrAction)
}


/**
 * Reset the [Availability] for all players.
 *
 * Developer's Commentary:
 * I am not 100% convinced this is the right approach. Availability is also reset at the beginning
 * of a teams turn, but right now having players marked as UNAVAILABLE when a drive ends, also means
 * they are rendered as not available during setup (since they haven't had a turn yet).
 */
fun getResetPlayerAvailabilityCommands(state: Game, rules: Rules): Array<Command> {
    val teams = listOf(state.homeTeam, state.awayTeam)
    val availableStatus = teams.flatMap { team ->
        team.map { player ->
            SetPlayerAvailability(player, Availability.AVAILABLE)
        }
    }
    return availableStatus.toTypedArray()
}

/**
 * Get commands that reset all Temporary Skills and Effects that apply to a single player.
 * See [getResetTeamTemporaryModifiersCommands] for resetting everything related to teams,
 * including players.
 */
fun getResetPlayerTemporaryModifiersCommands(
    state: Game,
    rules: Rules,
    player: Player,
    duration: Duration
): Array<Command> {
    val builder = mutableListOf<Command>()
    gatherResetPlayerTemporaryModifiersCommands(player, duration, builder)
    return builder.toTypedArray()
}

private fun gatherResetPlayerTemporaryModifiersCommands(
    player: Player,
    duration: Duration,
    builder: MutableList<Command>
) {
    // Find all temporary player stat characteristics modifiers
    val removableStatModifiers = player.statModifiers
        .filter { it.expiresAt == duration }
        .map { RemovePlayerStatModifier(player, it) }
    builder.addAll(removableStatModifiers)

    // Find all temporary player skills
    val removableSkills = player.extraSkills
        .filter { it.expiresAt == duration }
        .map { RemovePlayerSkill(player, it) }
    builder.addAll(removableSkills)

    // Reset skills that have been used
    val resetSkills = player.skills
        .filter { it.resetAt == duration }
        .map { SetSkillUsed(player, it, false) }
    builder.addAll(resetSkills)

    // Reset skill rerolls that have been used
    val resetSkillRerolls = player.skills
        .filterIsInstance<RerollSource>()
        .filter { it.rerollResetAt == duration }
        .map { SetSkillRerollUsed(it, false) }
    builder.addAll(resetSkillRerolls)

    // Shadowing is special as it tracks move usage, we need to reset its counter at the end of a turn
    val resetShadowingCounter = if (duration == Duration.END_OF_TURN) {
        val shadowingSkill = player.getSkillOrNull(SkillType.SHADOWING)
        if (shadowingSkill is com.jervisffb.engine.rules.bb2025.skills.Shadowing) {
            listOf(ResetShadowingSkill(player))
        } else {
            emptyList()
        }
    } else {
        emptyList()
    }
    builder.addAll(resetShadowingCounter)

    // Find all other temporary effects
    val removableTemporaryEffects = player.statusEffects
        .filter { it.duration == duration }
        .map { RemovePlayerStatusEffect(player, it) }
    builder.addAll(removableTemporaryEffects)
}

/**
 * Returns all commands that reset temporary table results, stat and skill modifiers for all teams.
 */
fun getResetTeamTemporaryModifiersCommands(
    state: Game,
    duration: Duration
): Array<Command> {
    if (duration in Duration.singlePlayerDurations) {
        error("Wrong use of API. Use `getResetPlayerTemporaryModifiersCommands` instead")
    }
    val builder = mutableListOf<Command>()
    val activeTeam = state.activeTeam ?: state.homeTeam
    val inactiveTeam = activeTeam.otherTeam()
    gatherResetTeamTemporaryModifiersCommands(activeTeam, duration, builder)
    if (duration != Duration.END_OF_OWN_TEAM_TURN) {
        gatherResetTeamTemporaryModifiersCommands(inactiveTeam, duration, builder)
    }
    return builder.toTypedArray()
}

// Collect all reset commands for a single team
private fun gatherResetTeamTemporaryModifiersCommands(
    team: Team,
    duration: Duration,
    builder: MutableList<Command>
) {
    val state = team.game

    // Find and reset all temporary state associated with a single player.
    team.forEach { player ->
        gatherResetPlayerTemporaryModifiersCommands(player, duration, builder)
    }

    // Remove all temporary rerolls that might have expired.
    // Note, Leader has a SPECIAL duration and is handled by itself
    val removableRerolls: List<RemoveTeamReroll> = team.rerolls
        .filter { it.duration == duration }
        .map { RemoveTeamReroll(team, it) }
    builder.addAll(removableRerolls)

    // Leader rerolls are only removed between normal halfs. They are kept
    // when entering overtime.
    // TODO Should the concept of CARRY_INTO_OVERTIME be lifted to Duration somehow?
    val removableLeaderRerolls = if (
        state.halfNo < state.rules.halfsPrGame && duration == Duration.END_OF_HALF
    ) {
        team.rerolls
            .filterIsInstance<LeaderTeamReroll>()
            .map { reroll -> RemoveTeamReroll(team, reroll) }
    } else {
        emptyList()
    }
    builder.addAll(removableLeaderRerolls)

    // Find all active Prayers of Nuffle that expires at the given duration
    val removablePrayers = team.activePrayersToNuffle
        .filter { it.duration == duration }
        .map {
            RemovePrayersToNuffle(team, it)
        }
    builder.addAll(removablePrayers)

    // All active special play cards that has ended their duration are marked
    // as played
    val specialPlayCards: List<SetSpecialPlayCardActive> = team.specialPlayCards
        .filter { it.isActive && duration == duration }
        .map { SetSpecialPlayCardActive(it, false) }
    builder.addAll(specialPlayCards)

    // Any Team Status Effects that might expire
    val teamStatusEffects: List<RemoveTeamStatusEffect> = team.statusEffects
        .filter { it.duration == duration }
        .map { RemoveTeamStatusEffect(team, it) }
    builder.addAll(teamStatusEffects)
}

/**
 * Returns all available spells across all wizards
 */
fun List<Wizard>.getAvailableSpells(trigger: Timing): List<Spell> {
    return this.flatMap { it.getAvailableSpells(trigger) }
}

fun List<SpecialPlayCard>.getAvailableCards(trigger: Timing, state: Game, rules: Rules): List<SpecialPlayCard> {
    return this
        .filter { it.triggers.contains(trigger) && !it.used }
        .filter { it.isApplicable(state, rules) }
}

fun List<InfamousCoachingStaff>.getAvailableAbilities(trigger: Timing, state: Game, rules: Rules): List<InfamousCoachAbility> {
    return this
        .flatMap { it.specialAbilities }
        .filter { it.triggers.contains(trigger) && !it.used }
        .filter { it.isApplicable(state, rules) }
}

/**
 * Calculate the estimated amount of moves a player has left. This includes using optional
 * skills like Sprint
 */
fun Player.estimatedMovesLeft(includeSprint: Boolean): Int {
    val sprintExtraMove = when (includeSprint && isSkillAvailable(SkillType.SPRINT)) {
        true -> SPRINT_EXTRA_RUSHES
        false -> 0
    }
    return movesLeft + rushesLeft + sprintExtraMove
}
