package com.jervisffb.tourplay.api

import kotlinx.serialization.Serializable
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.List

/**
 * This class contains the auto-generated classes representing the TourPlay REST API.
 * They have been generated using `tools/TourPlay.ipynb` notebook.
 */

// Mappings for https://tourplay.net/api/rosters/<rosterId>

@Serializable
public data class TourPlayRoster(
    public val id: Int,
    public val imageFile: String,
    public val apothecary: Boolean,
    public val assistantCoaches: Int,
    public val cheerLeaders: Int,
    public val fanFactor: Int,
    public val ruleSet: Int,
    public val necromancer: Boolean,
    public val reRolls: Int,
    public val shortTeamName: String,
    // public val sponsors: List<Any?>,
    public val teamColor: String,
    public val teamName: String,
    public val teamRace: String,
    public val treasury: Int,
    public val extraGoldQuantity: Int,
    public val rosterMaster: RosterMaster,
    public val teamSpecialRules: Int,
    public val hasMatchesInProgress: Boolean,
    public val hasMatchesPlayed: Boolean,
    public val isRedrafting: Boolean,
    public val partialPaymentStadium: Int,
    public val teamValue: Int,
    public val teamValueVanilla: Int,
    public val inducementsValue: Int,
    public val tournamentType: Int,
    public val isHiddenRoster: Boolean,
    public val isFrozenRoster: Boolean,
    // public val availableSponsors: List<Any?>,
    // public val availableStadiums: List<Any?>,
    public val hasSppAdvancement: Boolean,
    public val isUsingGoldPiecesBudget: Boolean,
    public val player: Player,
    public val coachRank: CoachRank,
    // public val squadRosters: List<Any?>,
    // public val inscriptions: List<Inscription>,
    // public val inducements: List<Any?>,
    public val hasOldPlayers: Boolean,
    public val lineUps: List<LineUp>,
    // public val lastMatches: List<LastMatch>,
)

@Serializable
public data class RosterMaster(
    public val teamRace: String,
    public val name: String,
    public val lineUpMasters: List<LineUpMaster>,
    public val prizeReRoll: Int,
    public val apothecary: Boolean,
    public val necromancer: Boolean,
    public val teamRosterType: Int,
    public val starPlayersMasters: List<StarPlayersMaster>,
    public val ruleSet: Int,
    public val teamSpecialRules: Int,
    public val selectableTeamSpecialRules: Int,
    public val tier: Int,
    public val maxBigGuys: Int,
    public val id: Int,
)

@Serializable
public data class LineUpMaster(
    public val rosterMasterId: Int,
    public val quantity: Int,
    public val position: String,
    public val cost: Int,
    public val ma: Int,
    public val st: Int,
    public val ag: Int,
    public val av: Int,
    public val pa: Int,
    public val skills: List<Skill>,
    public val skillNormal: Int,
    public val skillDouble: Int,
    public val iconVariation: Int,
    public val iconClass: String,
    public val availableRaces: Int,
    public val availableTeamSpecialRules: Int,
    public val ruleSet: Int,
    public val id: Int,
    public val isBigGuy: Boolean? = null,
) {
    val positionShortHand: String
        get() {
            // TODO This doesn't work with full Unicode. Revisit it, if it turns out to be a problem
            return position.trim()
                .split(Regex("\\s+"))
                .filter { it.isNotEmpty() }
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
}

@Serializable
public data class Skill(
    public val lineUpMasterId: Int,
    public val skillMasterId: Int, // = null,
    public val skillMaster: SkillMaster, // = null,
    public val id: Int,
    public val skillAttributeMasterId: Int? = null,
    public val skillAttributeMaster: SkillAttributeMaster? = null,
)

@Serializable
public data class SkillMaster(
    public val name: String,
    public val type: Int,
    public val ruleSet: Int,
    public val id: Int,
    public val skillAttributeMasterId: Int? = null,
    public val skillAttributeMaster: SkillAttributeMaster? = null,
)

@Serializable
public data class SkillAttributeMaster(
    public val type: Int,
    public val `value`: String,
    public val id: Int,
)

@Serializable
public data class StarPlayersMaster(
    public val quantity: Int,
    public val position: String,
    public val cost: Int,
    public val ma: Int,
    public val st: Int,
    public val ag: Int,
    public val av: Int,
    public val pa: Int,
    public val skills: List<Skill>,
    public val skillNormal: Int,
    public val skillDouble: Int,
    public val iconVariation: Int,
    public val iconClass: String,
    public val isStarPlayer: Boolean,
    public val availableRaces: Int,
    public val availableTeamSpecialRules: Int,
    public val ruleSet: Int,
    public val specialRuleName: String,
    public val id: Int,
    public val linkedWith: Int? = null,
)

@Serializable
public data class Player(
    public val applicationUserId: String,
    public val userNameToShow: String,
    public val pictureFileName: String,
    public val country: String,
)

@Serializable
public data class CoachRank(
    public val rankOverall: Int,
    public val previousRankOverall: Int,
    public val rankRegional: Int,
    public val previousRankRegional: Int,
    public val score: Double,
)

@Serializable
public data class Inscription(
    public val player: Player1,
    public val tournament: Tournament,
    public val category: Category,
)

@Serializable
public data class Player1(
    public val applicationUserId: String,
)

@Serializable
public data class Tournament(
    public val nameNormalized: String,
    public val name: String,
    public val state: Int,
    public val administrators: List<Administrator>,
)

@Serializable
public data class Administrator(
    public val applicationUserId: String,
    public val isCreator: Boolean,
)

@Serializable
public data class Category(
    public val id: Int,
    public val type: Int,
    public val phases: List<Phas>,
)

@Serializable
public data class Phas(
    public val categoryId: Int,
    public val type: Int,
    public val system: Int,
    public val state: Int,
    public val order: Int,
    public val confrontation: Int,
    public val pointsDraw: Double,
    public val pointsDefeat: Double,
    public val pointsBonusWin: Double,
    public val pointsPenaltyDefeat: Double,
    public val pointsBonusBye: Double,
    public val pointsWin: Double,
    public val pointsScoreMore3TDBonus: Double,
    public val pointsConcede0TDBonus: Double,
    public val pointsCauseMore3CASBonus: Double,
    public val pointsConcede: Double,
    public val pointsPerTouchdownBonus: Double,
    public val pointsPerCasualtyBonus: Double,
    public val pointsGamesPlayed: Double,
    public val winningsDedicatedFans: Boolean,
    public val winningsWin: Int,
    public val winningsDraw: Int,
    public val winningsDefeat: Int,
    public val winningsForTD: Int,
    public val winningsForCas: Int,
    public val winningsForDeaths: Int,
    public val specialCards: Boolean,
    public val mercenaries: Boolean,
    public val fabulousMercenaries: Boolean,
    public val rosteredMercenaries: Boolean,
    public val onlyAdminCreateMercenaries: Boolean,
    public val giantMercenary: Boolean,
    public val starPlayers: Boolean,
    public val famousStaff: Boolean,
    public val wizards: Boolean,
    public val biasedReferee: Boolean,
    public val mvp: Int,
    public val mvpCandidates: Int,
    public val withoutSP: Boolean,
    public val withoutDeaths: Boolean,
    public val expensiveMistakes: Boolean,
    public val rosteredStarPlayers: Boolean,
    public val referees: Boolean,
    public val spirallingExpenses: Boolean,
    public val roundName: String? = null,
    public val roundGeneration: Int? = null,
    public val weatherAvailables: List<Int>,
    public val hideStandings: Boolean,
    public val dedicatedFansWinIncrement: Boolean,
    // public val sponsorsAvailables: List<Any?>,
    public val stadiumsAvailables: List<Int>,
    public val hasStadiums: Boolean,
    public val hasSponsors: Boolean,
    public val pointsWinSquad: Double,
    public val pointsDrawSquad: Double,
    public val pointsDefeatSquad: Double,
    public val pointsBonusByeSquad: Double,
    public val pointsConcedeSquad: Double,
    public val injuryRecoveryAfterMatch: Boolean,
    public val id: Int,
    // public val secretObjectivesAvailable: List<Any?>? = null,
    public val excludeFromHonors: Boolean? = null,
    public val limitRepeatOpponent: Int? = null,
    public val limitRepeatRace: Int? = null,
    public val limitMatchesByTeam: Int? = null,
    public val coachesCanChallenge: Boolean? = null,
    public val swissSystem: Int? = null,
    public val useCustomBonusPoints: Boolean? = null,
)

@Serializable
public data class LineUp(
    public val ag: Int,
    public val av: Int,
    public val ma: Int,
    public val st: Int,
    public val pa: Int,
    public val cost: Int,
    public val iconClass: String,
    public val id: Int,
    public val lineUpMasterId: Int,
    public val lineUpMaster: LineUpMaster1,
    public val name: String,
    public val nigglingInjuries: Int,
    public val number: Int,
    public val pendingImprovements: Int,
    public val position: String,
    public val rosterId: Int,
    public val sessions: Int,
    public val level: Int,
    public val skills: List<Skill2>,
    public val starPlayerPoints: Int,
    public val state: Int,
    public val isActive: Boolean,
    public val canPlayNextGame: Boolean,
    public val isAvailableForStarPoints: Boolean,
    public val totalCasualties: Int,
    public val totalInjuries: Int,
    public val totalInterceptions: Int,
    public val totalMVP: Int,
    public val totalPass: Int,
    public val totalStarPlayerPoints: Int,
    public val totalTouchdowns: Int,
    public val isBigGuy: Boolean? = null,
)

@Serializable
public data class LineUpMaster1(
    public val iconClass: String,
    public val position: String,
    public val skillDouble: Int,
    public val skillNormal: Int,
    public val ag: Int,
    public val av: Int,
    public val ma: Int,
    public val st: Int,
    public val pa: Int,
    public val quantity: Int,
    public val availableRaces: Int,
    public val availableTeamSpecialRules: Int,
    public val id: Int,
    public val skills: List<Skill1>,
    public val cost: Int,
)

@Serializable
public data class Skill1(
    public val id: Int,
    public val lineUpMasterId: Int,
    public val skillMaster: SkillMaster1,
    public val skillMasterId: Int,
    public val isHidden: Boolean,
    public val skillAttributeMaster: SkillAttributeMaster? = null,
    public val skillAttributeMasterId: Int? = null,
)

@Serializable
public data class SkillMaster1(
    public val id: Int,
    public val name: String,
    public val type: Int,
)

@Serializable
public data class Skill2(
    public val id: Int,
    public val lineUpId: Int,
    public val skillMaster: SkillMaster1,
    public val skillMasterId: Int,
    public val isRandom: Boolean,
    public val isSecondary: Boolean,
    public val isHidden: Boolean,
    public val skillAttributeMaster: SkillAttributeMaster? = null,
)

@Serializable
public data class LastMatch(
    public val id: Int,
    public val state: Long,
    public val statePostMatch: Int,
    public val scores: List<Score>,
    public val inscriptionLocal: InscriptionLocal,
    public val inscriptionVisitor: InscriptionVisitor,
    public val groupName: String,
    public val groupsCount: Int,
    public val phaseType: Int,
    public val system: Int,
    public val categoryType: Int,
    public val tournament: Tournament1,
    public val round: Int,
    public val roundName: String? = null,
)

@Serializable
public data class Score(
    public val casualtiesLocal: Int,
    public val casualtiesVisitor: Int,
    public val concedeLocal: Boolean,
    public val concedeVisitor: Boolean,
    public val hasIncidence: Boolean,
    public val finishInstant: String,
    public val hasNoPointsInClassification: Boolean,
    public val scoreLocal: Int,
    public val scoreVisitor: Int,
)

@Serializable
public data class InscriptionLocal(
    public val roster: Roster,
    public val player: Player2,
)

@Serializable
public data class Roster(
    public val id: Int,
    public val teamColor: String,
    public val teamName: String,
    public val teamRace: String,
    public val shortTeamName: String,
    public val imageFile: String? = null,
)

@Serializable
public data class Player2(
    public val applicationUser: ApplicationUser,
)

@Serializable
public data class ApplicationUser(
    public val userNameToShow: String,
    public val pictureFileName: String,
    public val country: String,
    public val goldStarAwards: Int? = null,
)

@Serializable
public data class InscriptionVisitor(
    public val roster: Roster,
    public val player: Player2,
)

@Serializable
public data class Tournament1(
    public val name: String,
    public val nameNormalized: String,
)
