package com.jervisffb.engine.model

import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.SpecialPlayCard
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.model.modifiers.BrilliantCoachingModifiers
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.roster.SpecialRules
import com.jervisffb.engine.rules.common.tables.PrayerToNuffle
import com.jervisffb.engine.serialize.RosterLogo

class TeamHalfData(private val game: Game) {
    var totalRerolls: Int = 0
    var usedRerolls: Int = 0
}

class TeamDriveData(private val game: Game) {
    // Team related data
}

class TeamTurnData(private val game: Game) {
    var moveActions: Int
        get() = availableStandardActions[PlayerStandardActionType.MOVE]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.MOVE] = value
        }
    var passActions: Int
        get() = availableStandardActions[PlayerStandardActionType.PASS]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.PASS] = value
        }
    var handOffActions: Int
        get() = availableStandardActions[PlayerStandardActionType.HAND_OFF]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.HAND_OFF] = value
        }
    var blockActions: Int
        get() = availableStandardActions[PlayerStandardActionType.BLOCK]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.BLOCK] = value
        }
    var blitzActions: Int
        get() = availableStandardActions[PlayerStandardActionType.BLITZ]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.BLITZ] = value
        }
    var foulActions: Int
        get() = availableStandardActions[PlayerStandardActionType.FOUL]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.FOUL] = value
        }
    var secureTheBallActions: Int
        get() = availableStandardActions[PlayerStandardActionType.SECURE_THE_BALL]!!
        set(value) {
            availableStandardActions[PlayerStandardActionType.SECURE_THE_BALL] = value
        }

    val availableStandardActions =
        mutableMapOf(
            PlayerStandardActionType.MOVE to 0,
            PlayerStandardActionType.PASS to 0,
            PlayerStandardActionType.HAND_OFF to 0,
            PlayerStandardActionType.BLOCK to 0,
            PlayerStandardActionType.BLITZ to 0,
            PlayerStandardActionType.FOUL to 0,
            PlayerStandardActionType.SECURE_THE_BALL to 0,
        )

    val availableSpecialActions = mutableMapOf<PlayerSpecialActionType, Int>()
}

/**
 * Class modeling all state related to a Team.
 *
 * This class is not aware of any rules. Which changes are legal are determined by the relevant
 * [com.jervisffb.engine.fsm.Procedure].
 *
 * Also, most changes to this class should only happen through [com.jervisffb.engine.commands.Command]
 * objects, since it guarantees we can safely revert them again.
 */
class Team(
    val id: TeamId,
    val name: String,
    val type: GameType,
    val roster: BB2020Roster,
    var coach: Coach
) : Collection<Player> {

    // All players on the team
    val noToPlayer = mutableMapOf<PlayerNo, Player>()

    // Team staff
    var coachBanned: Boolean = false
    val brilliantCoachingModifiers = mutableListOf<BrilliantCoachingModifiers>()
    val apothecaries: Int // Limit
        get() = teamApothecaries.count { it.used } + tempApothecaries.count { it.used }
    val teamApothecaries = mutableListOf<Apothecary>()
    val tempApothecaries = mutableListOf<Apothecary>()
    fun getApothecaries(): List<Apothecary> = teamApothecaries + tempApothecaries

    // Track cheerleaders
    val cheerleaders: Int
        get() = teamCheerleaders + tempCheerleaders
    var teamCheerleaders: Int = 0 // 0-12
    var tempCheerleaders: Int = 0 // 0-4

    // Track assistant coaches
    val assistantCoaches: Int
        get() = teamAssistantCoaches + tempAssistantCoaches
    var teamAssistantCoaches: Int = 0
    var tempAssistantCoaches: Int = 0

    // Treasury
    var treasury: Int = 0
    var pettyCash: Int = 0

    // Fans
    var fanFactor: Int = 0
    var dedicatedFans: Int = 0

    // Team value (in total amount, e.g 100.000, not 100 K)
    var teamValue: Int = 0
    var currentTeamValue: Int = 0
    val specialRules = mutableListOf<SpecialRules>()
    // This just tracks the prayer itself, and not any effects it might have
    // caused. E.g., if Iron Man added the Mighty Blow skill to a player, a
    // temporary skill was added to that player. Which is cleaned up separately.
    val activePrayersToNuffle = mutableSetOf<PrayerToNuffle>()

    // Reroll tracking
    val rerolls = mutableListOf<TeamReroll>()
    var usedRerollThisTurn: Boolean = false
    val availableRerolls: List<TeamReroll>
        get() = rerolls.filter { !it.rerollUsed }
    val availableRerollCount: Int
        get() = availableRerolls.size

    // Inducements
    // TODO We should probably also have a section for "permanent inducements"
    //  While not in the rules, a lot of custom rulesets like NAF-style tournaments
    //  have them.
    var bloodweiserKegs: Int = 0
    val bribes = mutableListOf<Bribe>()
    val wizards = mutableListOf<Wizard>()
    val specialPlayCards = mutableListOf<SpecialPlayCard>()
    val infamousCoachingStaff = mutableListOf<InfamousCoachingStaff>()

    // Cyclic dependencies. Must be manually set when a Team is constructed
    // TODO Why do we have these and `isAwayTeam()`?
    lateinit var game: Game
    var teamIsHomeTeam: Boolean = false
    var teamIsAwayTeam: Boolean = false

    // Temporary state. Currently transient because we assume that
    // game state never needs to be deserialized directly, but is only
    // created by running forward or backwards through all game actions
    // This API probably needs to be redesigned
    lateinit var halfData: TeamHalfData
    lateinit var driveData: TeamDriveData
    lateinit var turnData: TeamTurnData
    var turnMarker: Int = 0

    // TODO Add support for custom team logos that are different from
    //  the roster logo.
    var teamLogo: RosterLogo? = null

    // Must be called before using this class.
    // Used to break circular reference between Team and Game instances
    fun setGameReference(game: Game) {
        halfData = TeamHalfData(game)
        driveData = TeamDriveData(game)
        turnData = TeamTurnData(game)
        this.game = game
        teamIsHomeTeam = (game.homeTeam == this)
        teamIsAwayTeam = (game.awayTeam == this)
    }

    fun otherTeam(): Team {
        return if (game.homeTeam == this) {
            game.awayTeam
        } else {
            game.homeTeam
        }
    }

    fun isHomeTeam(): Boolean = (game.homeTeam == this)

    fun isAwayTeam(): Boolean = (game.awayTeam == this)

    fun add(player: Player) {
        player.team = this
        noToPlayer[player.number] = player
    }

    operator fun get(playerNo: PlayerNo): Player = noToPlayer[playerNo] ?: throw IllegalArgumentException("Player $playerNo not found")
    operator fun get(playerId: PlayerId): Player = noToPlayer.values.firstOrNull { it.id == playerId } ?: throw IllegalArgumentException("Player $playerId not found")
    fun getOrNull(playerNo: PlayerNo): Player? = noToPlayer[playerNo]

    override val size: Int
        get() = noToPlayer.size

    override fun isEmpty(): Boolean = noToPlayer.isEmpty()

    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()

    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)

    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)

    fun hasPrayer(prayer: PrayerToNuffle): Boolean {
        return activePrayersToNuffle.contains(prayer)
    }
}
