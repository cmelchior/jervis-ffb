package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemovePlayerSkill
import com.jervisffb.engine.commands.RemovePlayerStatModifier
import com.jervisffb.engine.commands.RemovePlayerTemporaryEffect
import com.jervisffb.engine.commands.RemovePrayersToNuffle
import com.jervisffb.engine.commands.RemoveTeamReroll
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetSpecialPlayCardActive
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.InfamousCoachAbility
import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.SpecialPlayCard
import com.jervisffb.engine.model.inducements.Spell
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Sprint

/**
 * Returns the [Command] for setting the available number of rushes for this action.
 */
fun getSetPlayerRushesCommand(rules: Rules, player: Player): Command {
    // We unconditionally use Sprint as the coach can just decide _not_ to use
    // the extra move. Which is faster than us spending time asking to use the
    // skill or not.
    // TODO This is not correct. When combined with Frenzy, there might be cases
    //  where you do not want to use Sprint.
    val rushesPrAction = if (player.hasSkill<Sprint>()) rules.rushesPrAction + 1 else rules.rushesPrAction
    return SetPlayerRushesLeft(player, rushesPrAction)
}

/**
 * Returns all commands that reset temporary table results, stat and skill modifiers for the
 * current active team.
 */
fun getResetTemporaryModifiersCommands(state: Game, rules: Rules, duration: Duration): Array<Command> {
    val teams = listOf(state.homeTeam, state.awayTeam)

    // Find all temporary player stat characteristics modifiers
    val removableStatModifiers = teams.flatMap { team ->
        team.flatMap { player ->
            player.getStatModifiers()
                .filter { it.expiresAt == duration }
                .map { RemovePlayerStatModifier(player, it) }
        }
    }

    // Find all temporary player skills
    val removableSkills = teams.flatMap { team ->
        team.flatMap { player ->
            player.extraSkills
                .filter { it.expiresAt == duration }
                .map { RemovePlayerSkill(player, it) }
        }
    }

    // Find all other temporary effects
    val removableTemporaryEffects = teams.flatMap { team ->
        team.flatMap { player ->
            player.temporaryEffects
                .filter { it.duration == duration }
                .map { RemovePlayerTemporaryEffect(player, it) }
        }
    }

    // Remove all temporary rerolls that might have expired
    // TODO Consider overtime here
    // TODO What about Leader?
    val removableRerolls: List<RemoveTeamReroll> = teams.flatMap { team ->
        team.rerolls
            .filter { it.duration == duration }
            .map { RemoveTeamReroll(team, it) }
    }

    // Find all active Prayers of Nuffle that expires at the given duration
    val removablePrayers = teams.flatMap { team ->
        team.activePrayersToNuffle
            .filter { it.duration == duration }
            .map { RemovePrayersToNuffle(team, it) }
    }

    // All active special play cards that has ended their duration are marked
    // as played
    val specialPlayCards: List<SetSpecialPlayCardActive> = teams.flatMap { team ->
        team.specialPlayCards
            .filter { it.isActive && duration == duration }
            .map { SetSpecialPlayCardActive(it, false) }
    }

    return (
        removableStatModifiers +
        removableSkills +
        removableRerolls +
        removablePrayers
    ).toTypedArray()
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


