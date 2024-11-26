package com.jervisffb.engine.model

import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.SpecialPlayCard
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.SpecialRules
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.utils.safeTryEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.JvmInline
import kotlin.properties.Delegates
import kotlin.random.Random

@Serializable
@JvmInline
value class TeamId(val value: String = "")

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
    val availableStandardActions =
        mutableMapOf(
            PlayerStandardActionType.MOVE to 0,
            PlayerStandardActionType.PASS to 0,
            PlayerStandardActionType.HAND_OFF to 0,
            PlayerStandardActionType.BLOCK to 0,
            PlayerStandardActionType.BLITZ to 0,
            PlayerStandardActionType.FOUL to 0,
        )

    val availableSpecialActions = mutableMapOf<PlayerSpecialActionType, Int>()
}

@Serializable
class Team(val name: String, val roster: BB2020Roster/*, val coach: Coach*/) : Collection<Player>, Observable<Team>() {
    val noToPlayer = mutableMapOf<PlayerNo, Player>()

    // Fixed Team data, identifying the team
    val id = TeamId("team-${Random.nextLong()}")

    // Team staff
    var coachBanned: Boolean = false
    val apothecaries: Int // Limit
        get() = teamApothecaries.count { it.used } + tempApothecaries.count { it.used }
    val teamApothecaries = mutableListOf<Apothecary>()
    val tempApothecaries = mutableListOf<Apothecary>()
    fun getApothecaries(): List<Apothecary> = teamApothecaries + tempApothecaries

    // Track cheerleaders
    val cheerLeaders: Int
        get() = teamCheerleaders + tempCheerleaders
    var teamCheerleaders: Int = 0 // 0-12
    var tempCheerleaders: Int = 0 // 0-4

    // Track assistant coaches
    val assistantCoaches: Int
        get() = teamAssistentCoaches + tempAssistantCoaches
    var teamAssistentCoaches: Int = 0
    var tempAssistantCoaches: Int = 0

    // Treasury
    var treasury: Int = 0
    var pettyCash: Int = 0

    // Fans
    var fanFactor: Int = 0
    var dedicatedFans: Int = 0

    var teamValue: Int = 0
    val specialRules = mutableListOf<SpecialRules>()
    val activePrayersToNuffle = mutableSetOf<PrayerToNuffle>()

    // Reroll tracking
    val rerolls = mutableListOf<TeamReroll>()
    var usedRerollThisTurn: Boolean = false
    val availableRerolls: List<TeamReroll>
        get() = rerolls.filter { !it.rerollUsed }
    val availableRerollCount: Int
        get() = availableRerolls.size

    // Inducements
    var bloodweiserKegs: Int = 0
    val bribes = mutableListOf<Bribe>()
    val wizards = mutableListOf<Wizard>()
    val specialPlayCards = mutableListOf<SpecialPlayCard>()
    val infamousCoachingStaff = mutableListOf<InfamousCoachingStaff>()

    // Special team state that needs to be tracked for the given period
    @Transient
    lateinit var game: Game

    @Transient
    lateinit var halfData: TeamHalfData

    @Transient
    lateinit var driveData: TeamDriveData

    @Transient
    lateinit var turnData: TeamTurnData

    var turnMarker by Delegates.observable(0) { prop, old, new ->
//        game.gameFlow.safeTryEmit(game)
    }

    // Must be called before using this class.
    // Used to break circular reference between Team and Game instances
    fun setGameReference(game: Game) {
        halfData = TeamHalfData(game)
        driveData = TeamDriveData(game)
        turnData = TeamTurnData(game)
        this.game = game
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

    override val size: Int
        get() = noToPlayer.size

    override fun isEmpty(): Boolean = noToPlayer.isEmpty()

    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()

    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)

    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)

    fun hasPrayer(prayer: PrayerToNuffle): Boolean {
        return activePrayersToNuffle.contains(prayer)
    }

    fun notifyDogoutChange() {
        val playersInDogout = noToPlayer.values.filter { it.location == DogOut }
        _dogoutState.safeTryEmit(playersInDogout)
    }

    @Transient
    private val _dogoutState =
        MutableSharedFlow<List<Player>>(replay = 1, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.SUSPEND)

    @Transient
    val dogoutFlow: SharedFlow<List<Player>> = _dogoutState
}
