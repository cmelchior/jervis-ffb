package com.jervisffb.fumbbl.net.utils

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.teamBuilder
import com.jervisffb.fumbbl.net.model.SpecialRule
import com.jervisffb.resources.CHAOS_DWARF_TEAM
import com.jervisffb.resources.ELVEN_UNION_TEAM
import com.jervisffb.resources.HUMAN_TEAM
import com.jervisffb.resources.KHORNE_TEAM
import com.jervisffb.resources.SKAVEN_TEAM

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
    val field: Field = extractField(game.fieldModel)
    return Game(rules, homeTeam, awayTeam, field)
}

private fun extractTeam(rules: Rules, team: FumbblTeam): Team {
    val roster = extractRoster(team.roster)
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
                    SpecialRule.SYLVANIAN_SPOTLIGHT -> RegionalSpecialRule.SYLVIAN_SPOTLIGHT
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
            if (position == null) {
                throw IllegalStateException(
                    "Could not find position '${fumbblPosition.positionName}' in '${team.roster.rosterName}'",
                )
            }
            val skills = fumbblPlayer.skillValuesMap.map {
                convertFumbblSkillToSkillId(rules, it.key)
            }.filterNotNull()

            addPlayer(
                PlayerId(fumbblPlayer.playerId),
                fumbblPlayer.playerName,
                PlayerNo(fumbblPlayer.playerNr),
                position,
                skills
            )
        }
    }
}

private fun extractRoster(roster: FumbblRoster): BB2020Roster {
    // TODO Add logic for building custom rosters, for now
    //  just refer to the original rules
    return when (roster.rosterName) {
        "Chaos Dwarf" -> CHAOS_DWARF_TEAM
        "Human" -> HUMAN_TEAM
        "Khorne" -> KHORNE_TEAM
        "Elven Union" -> ELVEN_UNION_TEAM
        "Skaven" -> SKAVEN_TEAM
        else -> TODO("Missing team: ${roster.rosterName}")
    }
}

private fun extractField(field: FumbblField): Field {
    // TODO Extract more information when we know what to fetch
    return Field(width = 26, height = 15)
}

/**
 * Map FUMBBL Skill Names to Jervis [SkillId]s.
 * Return `null` if the name could not be mapped or if the skill isn't supported
 * by the ruletset.
 */
fun convertFumbblSkillToSkillId(rules: Rules, skillName: String): SkillId? {
    // We should probably hard code all the FUMBBL titles instead of hoping the names are the same.
    // But for now, we just do it in the few places with known problems and hope for the best.
    val normalizedSkillName = when (skillName) {
        "Side Step" -> SkillType.SIDESTEP.description
        else -> skillName
    }
    return rules.skillSettings.getSkillId(normalizedSkillName)
}


