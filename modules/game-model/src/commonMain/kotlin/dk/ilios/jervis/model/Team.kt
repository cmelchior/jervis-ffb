package dk.ilios.jervis.model

import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.SpecialRules
import dk.ilios.jervis.rules.skills.TeamReroll
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import dk.ilios.jervis.utils.safeTryEmit
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
value class TeamId(val id: String = "")

class TeamHalfData(private val game: Game) {
    var totalRerolls: Int = 0
    var usedRerolls: Int = 0
}

class TeamDriveData(private val game: Game) {
    // Team related data
}

class TeamTurnData(private val game: Game) {
    var currentTurn by Delegates.observable(0) { prop, old, new ->
        game.gameFlow.safeTryEmit(game)
    }
    var moveActions: Int
        get() = availableActions[PlayerActionType.MOVE]!!
        set(value) {
            availableActions[PlayerActionType.MOVE] = value
        }
    var passActions: Int
        get() = availableActions[PlayerActionType.PASS]!!
        set(value) {
            availableActions[PlayerActionType.PASS] = value
        }
    var handOffActions: Int
        get() = availableActions[PlayerActionType.HAND_OFF]!!
        set(value) {
            availableActions[PlayerActionType.HAND_OFF] = value
        }
    var blockActions: Int
        get() = availableActions[PlayerActionType.BLOCK]!!
        set(value) {
            availableActions[PlayerActionType.BLOCK] = value
        }
    var blitzActions: Int
        get() = availableActions[PlayerActionType.BLITZ]!!
        set(value) {
            availableActions[PlayerActionType.BLITZ] = value
        }
    var foulActions: Int
        get() = availableActions[PlayerActionType.FOUL]!!
        set(value) {
            availableActions[PlayerActionType.FOUL] = value
        }
    val availableActions =
        mutableMapOf(
            PlayerActionType.MOVE to 0,
            PlayerActionType.PASS to 0,
            PlayerActionType.HAND_OFF to 0,
            PlayerActionType.BLOCK to 0,
            PlayerActionType.BLITZ to 0,
            PlayerActionType.FOUL to 0,
        )
}

class TeamTemporaryData(private val game: Game) {
    // This contain the result of the last dice rolled
    val dieRoll = mutableListOf<DieResult>()
}

@Serializable
class Team(val name: String, val roster: Roster, val coach: Coach) : Collection<Player>, Observable<Team>() {
    val noToPlayer = mutableMapOf<PlayerNo, Player>()

    // Fixed Team data, identifying the team
    val id = TeamId("team-${Random.nextLong()}")

    // Variable team data that might change during the game
    var rerollsCountOnRoster: Int = 0

    @Transient
    var rerolls: MutableList<TeamReroll> = mutableListOf()

    @Transient
    val availableRerolls: List<TeamReroll>
        get() = rerolls.filter { !it.rerollUsed }

    @Transient
    val availableRerollCount: Int
        get() = availableRerolls.size

    @Transient
    var usedTeamRerollThisTurn: Boolean = false

    var coachBanned: Boolean = false
    var apothecaries: Int = 0
    var cheerLeaders: Int = 0
    var assistentCoaches: Int = 0
    var fanFactor: Int = 0
    var teamValue: Int = 0
    var treasury: Int = 0
    var dedicatedFans: Int = 0
    val specialRules = mutableListOf<SpecialRules>()
    val activePrayersOfNuffle = mutableSetOf<PrayerToNuffle>()

    // Special team state that needs to be tracked for the given period
    @Transient
    lateinit var game: Game

    @Transient
    lateinit var halfData: TeamHalfData

    @Transient
    lateinit var driveData: TeamDriveData

    @Transient
    lateinit var turnData: TeamTurnData

    @Transient
    lateinit var temporaryData: TeamTemporaryData




    // Must be called before using this class.
    // Used to break circular reference between Team and Game instances
    fun setGameReference(game: Game) {
        halfData = TeamHalfData(game)
        driveData = TeamDriveData(game)
        turnData = TeamTurnData(game)
        temporaryData = TeamTemporaryData(game)
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

    operator fun get(playerNo: PlayerNo): Player? = noToPlayer[playerNo]

    override val size: Int
        get() = noToPlayer.size

    override fun isEmpty(): Boolean = noToPlayer.isEmpty()

    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()

    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)

    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)

    fun hasPrayer(prayer: PrayerToNuffle): Boolean {
        return activePrayersOfNuffle.contains(prayer)
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
