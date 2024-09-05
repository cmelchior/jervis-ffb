package dk.ilios.jervis.procedures

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemovePlayerSkill
import dk.ilios.jervis.commands.RemovePlayerStatModifier
import dk.ilios.jervis.commands.RemovePrayersToNuffle
import dk.ilios.jervis.commands.SetPlayerRushesLeft
import dk.ilios.jervis.commands.SetSpecialPlayCardActive
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.model.inducements.InfamousCoachAbility
import dk.ilios.jervis.model.inducements.InfamousCoachingStaff
import dk.ilios.jervis.model.inducements.SpecialPlayCard
import dk.ilios.jervis.model.inducements.Spell
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.model.inducements.wizards.Wizard
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Sprint

/**
 * Returns the [Command] for setting the available number of rushes for this action.
 */
fun getSetPlayerRushesCommand(rules: Rules, player: Player): Command {
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


