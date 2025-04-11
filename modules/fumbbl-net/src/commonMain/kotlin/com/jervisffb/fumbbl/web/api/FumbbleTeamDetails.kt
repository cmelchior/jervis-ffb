package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class FumbbleTeamDetails(
    public val id: Int,
    public val coach: Coach,
    public val roster: Roster,
    public val name: String,
    public val bio: Bio,
    public val divisionId: Int,
    public val division: String,
    public val league: Int,
    public val rerolls: Int,
    public val ruleset: Int,
    public val status: String,
    public val teamValue: Int,
    public val currentTeamValue: Int,
    public val treasury: Int,
    public val fanFactor: Int,
    public val assistantCoaches: Int,
    public val cheerleaders: Int,
    public val apothecary: String,
    public val record: Record,
    // This can be both an Array (if no rules) and an object if it has special rules.
    // But when it is an object, it is the object keys that define the name of the Special
    // Rule. Rather than trying to handle the mapping here, we just expose the underlying
    // JSON and handle it later.
    public val specialRules: JsonElement,
    public val seasonInfo: SeasonInfo,
    public val tvLimit: Int,
    public val options: Options,
    public val skillLimits: SkillLimits,
    public val redrafting: Redrafting,
    public val redraftingLimits: RedraftingLimits,
    public val players: List<Player>,
//    public val firedPlayers: List<Any?>,
//    public val debug: List<Any?>,
)

@Serializable
public data class Coach(
    public val id: Int,
    public val name: String,
)

@Serializable
public data class Roster(
    public val id: Int,
    public val name: String,
)

@Serializable
public data class Bio(
    public val image: Int,
    public val htmlBio: String,
)

@Serializable
public data class Record(
    public val games: Int,
    public val wins: Int,
    public val ties: Int,
    public val losses: Int,
    public val form: String,
    public val td: Td,
    public val cas: Cas,
)

@Serializable
public data class Td(
    public val delta: Int,
    public val `for`: Int,
    public val against: Int,
)

@Serializable
public data class Cas(
    public val delta: Int,
    public val `for`: Int,
    public val against: Int,
)

// It looks like special rules has the option for futher customization.
// It is a bit unclear what it is used for. For now, we only care about
// the keys anyway. Actually, the Special Rules are read from the Roster
// right now. It is a bit unclear if the Team Special Rules could differ
// from these?
@Serializable
public data class SpecialRules(
    @SerialName("Old World Classic")
    public val oldWorldClassic: List<String?>? = null,
)

@Serializable
public data class SeasonInfo(
    public val currentSeason: Int,
    public val gamesPlayedInCurrentSeason: Int,
    public val record: Record1,
)

@Serializable
public data class Record1(
    public val wins: Int,
    public val ties: Int,
    public val losses: Int,
)

@Serializable
public data class Options(
    public val crossLeagueMatches: Boolean,
)

@Serializable
public data class SkillLimits(
    public val categories: List<Category>,
    public val spp: List<List<Int>>,
)

@Serializable
public data class Category(
    public val id: String,
    public val description: String,
    public val cost: String,
)

@Serializable
public data class Redrafting(
    public val base: Int,
    public val goldPerGame: Int,
    public val goldPerWin: Int,
    public val goldPerTie: Int,
    public val goldPerLoss: Int,
    public val redraftRamp: Int,
    public val redraftCap: Int,
    public val seasonGames: Int,
    public val tooltip: String,
    public val cappedBudget: Int,
    public val budgetCap: Int,
)

@Serializable
public data class RedraftingLimits(
    public val budget: Int,
    public val treasury: Int,
    public val newTreasury: Int,
    public val rerolls: Int,
    public val fans: Int,
    public val coaches: Int,
    public val cheerleaders: Int,
    public val apothecary: Int,
)

@Serializable
public data class Player(
    public val id: Int,
    public val number: Int,
    public val status: Int,
    public val name: String,
    public val gender: String?,
    public val hasBio: Boolean,
    public val position: String,
    public val positionId: Int,
    public val record: Record2,
    public val skillStatus: SkillStatus,
    public val injuries: String,
    public val skills: List<String?>,
    public val skillCosts: List<Int>,
    public val refundable: Boolean,
)

@Serializable
public data class Record2(
    public val seasons: Int,
    public val games: Int,
    public val completions: Int,
    public val touchdowns: Int,
    public val deflections: Int,
    public val interceptions: Int,
    public val casualties: Int,
    public val mvps: Int,
    public val spp: Int,
    @SerialName("extra_spp")
    public val extraSpp: Int,
    @SerialName("spent_spp")
    public val spentSpp: Int,
)

@Serializable
public data class SkillStatus(
    public val status: String?,
    public val maxLimit: Int?,
    public val tier: Int?,
    // public val numRewards: Any?,
)
