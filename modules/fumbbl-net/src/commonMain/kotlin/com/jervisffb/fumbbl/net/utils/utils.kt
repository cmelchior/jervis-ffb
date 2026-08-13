package com.jervisffb.fumbbl.net.utils

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Pitch
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.teamBuilder
import com.jervisffb.fumbbl.net.model.SpecialRule
import com.jervisffb.resources.bb2020.AMAZON_TEAM_BB2020
import com.jervisffb.resources.bb2020.CHAOS_DWARF_TEAM_BB2020
import com.jervisffb.resources.bb2020.ELVEN_UNION_TEAM_BB2020
import com.jervisffb.resources.bb2020.HUMAN_TEAM_BB2020
import com.jervisffb.resources.bb2020.KHORNE_TEAM_BB2020
import com.jervisffb.resources.bb2020.LIZARDMEN_TEAM_BB2020
import com.jervisffb.resources.bb2020.ORC_TEAM_BB2020
import com.jervisffb.resources.bb2020.SKAVEN_TEAM_BB2020
import com.jervisffb.resources.bb2025.AMAZON_TEAM_BB2025
import com.jervisffb.resources.bb2025.CHAOS_DWARF_TEAM_BB2025
import com.jervisffb.resources.bb2025.DWARF_TEAM_BB2025
import com.jervisffb.resources.bb2025.ELVEN_UNION_TEAM_BB2025
import com.jervisffb.resources.bb2025.HIGH_ELF_TEAM_BB2025
import com.jervisffb.resources.bb2025.HUMAN_TEAM_BB2025
import com.jervisffb.resources.bb2025.KHORNE_TEAM_BB2025
import com.jervisffb.resources.bb2025.LIZARDMEN_TEAM_BB2025
import com.jervisffb.resources.bb2025.NURGLE_TEAM_BB2025
import com.jervisffb.resources.bb2025.ORC_TEAM_BB2025
import com.jervisffb.resources.bb2025.SKAVEN_TEAM_BB2025
import com.jervisffb.resources.bb2025.TOMB_KINGS_TEAM_BB2025

typealias FumbblGame = com.jervisffb.fumbbl.net.model.Game
typealias FumbblTeam = com.jervisffb.fumbbl.net.model.Team
typealias FumbblField = com.jervisffb.fumbbl.net.model.FieldModel
typealias FumbblRoster = com.jervisffb.fumbbl.net.model.Roster
typealias FumbblPlayer = com.jervisffb.fumbbl.net.model.Player
typealias FumbblCoordinate = com.jervisffb.fumbbl.net.model.FieldCoordinate

/**
 * Convert a FUMBBL Game Model into the equivalent Jervis Game Model.
 *
 * This can be used to bootstrap the game model from FUMBBL Replay files.
 *
 * @see [com.jervisffb.fumbbl.net.api.commands.ServerCommandReplay].
 */
fun Game.Companion.fromFumbblState(rules: Rules, game: FumbblGame): Game {
    val homeTeam = extractTeam(rules, game.teamHome)
    val awayTeam = extractTeam(rules, game.teamAway)
    return Game(rules, homeTeam, awayTeam)
}

private fun extractTeam(rules: Rules, team: FumbblTeam): Team {
    val roster = extractRoster(rules, team.roster)
    return teamBuilder(rules, roster) {
        this.name = team.teamName
        this.coach = Coach(id = CoachId(team.coach), name = team.coach)
        // val race Something we care about?
        // val baseIconPath: String, // This is relevant for the UI model. Figure out how to include this
        // val logoUrl: String?, // This is relevant for the UI model. Figure out how to include this
        this.rerolls = team.reRolls
        this.apothecaries = team.apothecaries
        this.cheerleaders = team.cheerleaders
        this.assistentCoaches = team.assistantCoaches
        this.fanFactor = team.fanFactor
        this.teamValue = team.teamValue
        this.dedicatedFans = team.dedicatedFans
        team.specialRules.forEach {
            val specialRule =
                when (it) {
                    SpecialRule.BADLANDS_BRAWL -> RegionalSpecialRule.BADLANDS_BRAWL
                    SpecialRule.BRIBERY_AND_CORRUPTION -> TeamSpecialRule.BRIBERY_AND_CORRUPTION
                    SpecialRule.ELVEN_KINGDOMS_LEAGUE -> RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
                    SpecialRule.FAVOURED_OF_KHORNE -> TeamSpecialRule.FAVOURED_OF_KHORNE
                    SpecialRule.FAVOURED_OF_NURGLE -> TeamSpecialRule.FAVOURED_OF_NURGLE
                    SpecialRule.FAVOURED_OF_SLAANESH -> TeamSpecialRule.FAVOURED_OF_SLAANESH
                    SpecialRule.FAVOURED_OF_TZEENTCH -> TeamSpecialRule.FAVOURED_OF_TZEENTCH
                    SpecialRule.FAVOURED_OF_UNDIVIDED -> TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
                    SpecialRule.HALFLING_THIMBLE_CUP -> RegionalSpecialRule.HAFLING_THIMBLE_CUP
                    SpecialRule.LOW_COST_LINEMEN -> TeamSpecialRule.LOW_COST_LINEMEN
                    SpecialRule.LUSTRIAN_SUPERLEAGUE -> RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE
                    SpecialRule.MASTERS_OF_UNDEATH -> TeamSpecialRule.MASTERS_OF_UNDEATH
                    SpecialRule.OLD_WORLD_CLASSIC -> RegionalSpecialRule.OLD_WORLD_CLASSIC
                    SpecialRule.SYLVANIAN_SPOTLIGHT -> RegionalSpecialRule.SYLVANIAN_SPOTLIGHT
                    SpecialRule.UNDERWORLD_CHALLENGE -> RegionalSpecialRule.UNDERWORLD_CHALLENGE
                    SpecialRule.WORLDS_EDGE_SUPERLEAGUE -> RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE
                }
            this.specialRules.add(specialRule)
        }
        team.players.forEach { fumbblPlayer: FumbblPlayer ->
            val fumbblPosition = team.roster.positions.firstOrNull { it.positionId == fumbblPlayer.positionId }
            if (fumbblPosition == null) {
                throw IllegalStateException("Could not find matching position: ${fumbblPlayer.positionId}")
            }
            val position = roster.positions.firstOrNull { it.titleSingular == fumbblPosition.positionName }
                ?: roster.positions.firstOrNull { it.titleSingular == mapFumbblPositionName(fumbblPosition.positionName) }
            if (position == null) {
                throw IllegalStateException(
                    "Could not find position '${fumbblPosition.positionName}' in '${team.roster.rosterName}'",
                )
            }
            // FUMBBL lists a player's starting (positional) and earned skills
            // together. The roster position's skillArray holds the positional
            // skills, so anything beyond those is an extra skill.
            val positionalSkillTypes = fumbblPosition.skillArray
                .mapNotNull { convertFumbblSkillToSkillId(rules, it)?.type }
                .toSet()
            val extraSkills = fumbblPlayer.skillArray
                .mapNotNull { convertFumbblSkillToSkillId(rules, it) }
                .filter { it.type !in positionalSkillTypes }

            addPlayer(
                PlayerId(fumbblPlayer.playerId),
                fumbblPlayer.playerName,
                PlayerNo(fumbblPlayer.playerNr),
                position,
                extraSkills
            )
        }
    }
}

private fun extractRoster(rules: Rules, roster: FumbblRoster): Roster {
    // TODO Add logic for building custom rosters, for now
    //  just refer to the original rules
    val bb2025 = rules.baseVersion == GameVersion.BB2025
    return when (roster.rosterName) {
        "Amazon" -> if (bb2025) AMAZON_TEAM_BB2025 else AMAZON_TEAM_BB2020
        "Chaos Dwarf" -> if (bb2025) CHAOS_DWARF_TEAM_BB2025 else CHAOS_DWARF_TEAM_BB2020
        "Dwarf" -> DWARF_TEAM_BB2025 // Dwarf is BB2025-only in Jervis
        "Elven Union" -> if (bb2025) ELVEN_UNION_TEAM_BB2025 else ELVEN_UNION_TEAM_BB2020
        "High Elf" -> HIGH_ELF_TEAM_BB2025 // High Elf is BB2025-only in Jervis
        "Human" -> if (bb2025) HUMAN_TEAM_BB2025 else HUMAN_TEAM_BB2020
        "Khorne" -> if (bb2025) KHORNE_TEAM_BB2025 else KHORNE_TEAM_BB2020
        "Lizardmen" -> if (bb2025) LIZARDMEN_TEAM_BB2025 else LIZARDMEN_TEAM_BB2020
        "Nurgle" -> NURGLE_TEAM_BB2025 // Nurgle is BB2025-only in Jervis
        "Orc" -> if (bb2025) ORC_TEAM_BB2025 else ORC_TEAM_BB2020
        "Skaven" -> if (bb2025) SKAVEN_TEAM_BB2025 else SKAVEN_TEAM_BB2020
        "Tomb Kings" -> TOMB_KINGS_TEAM_BB2025 // Tomb Kings is BB2025-only in Jervis
        else -> TODO("Missing team: ${roster.rosterName}")
    }
}

private fun extractField(field: FumbblField): Pitch {
    // TODO Extract more information when we know what to fetch
    return Pitch(width = 26, height = 15)
}

/**
 * Maps FUMBBL position names to their Jervis equivalents. FUMBBL occasionally
 * renames positions relative to the official rulebook; this bridges those gaps
 * so the roster position lookup in [extractTeam] can match.
 */
private fun mapFumbblPositionName(positionName: String): String = when (positionName) {
    "Anointed Blitzer" -> "Tomb Kings Blitzer"
    "Anointed Thrower" -> "Tomb Kings Thrower"
    "Chaos Dwarf Flamesmith" -> "Flamesmith"
    "Dwarf Blocker Lineman" -> "Dwarf Lineman"
    "Elf Blitzer" -> "Blitzer"
    "Elf Catcher" -> "Catcher"
    "Elf Lineman" -> "Lineman"
    "Elf Thrower" -> "Thrower"
    "Hobgoblin Sneaky Stabba" -> "Sneaky Stabba"
    "Human Blitzer" -> "Blitzer"
    "Human Catcher" -> "Catcher"
    "Human Thrower" -> "Thrower"
    "Minotaur" -> "Enslaved Minotaur"
    "Skaven Clanrat" -> "Skaven Clanrat Lineman"
    "Skaven Thrower" -> "Thrower"
    "Skink Lineman" -> "Skink Runner Lineman"
    else -> positionName
}

/**
 * Map FUMBBL Skill Names to Jervis [SkillId]s.
 * Return `null` if the name could not be mapped or if the skill isn't supported
 * by the ruleset.
 */
fun convertFumbblSkillToSkillId(rules: Rules, fumbblSkillName: String): SkillId? {
    // We should probably hard code all the FUMBBL titles instead of hoping the names are the same.
    // But for now, we just do it in the few places with known problems and hope for the best.
    val normalizedSkillName = when (fumbblSkillName) {
        "Side Step" -> SkillType.SIDESTEP.description
        else -> fumbblSkillName
    }
    return rules.skillSettings.getSkillIdFromNiceDescription(normalizedSkillName)
}


