package com.jervis.fumbbl.restapi

import kotlinx.serialization.Serializable

// Type safe wrapper for https://fumbbl.com/apidoc/#/team/get_team_get__teamId_
@Serializable
data class Team(
    val apothecary: String,
    val assistantCoaches: Int,
//    val bio: Bio,
    val cheerleaders: Int,
    val coach: Coach,
    val currentTeamValue: Int,
    val division: String,
    val divisionId: Int,
    val fanFactor: Int,
//    val firedPlayers: List<Any>,
    val id: Int,
    val league: Int,
    val name: String,
//    val options: Options,
    val players: List<Player>,
//    val record: RecordX,
//    val redrafting: Redrafting,
//    val redraftingLimits: RedraftingLimits,
    val rerolls: Int,
    val roster: Roster,
    val ruleset: Int,
//    val seasonInfo: SeasonInfo,
//    val skillLimits: SkillLimits,
//    val specialRules: SpecialRules,
    val status: String,
    val teamValue: Int,
    val treasury: Int,
    val tvLimit: Int,
)
