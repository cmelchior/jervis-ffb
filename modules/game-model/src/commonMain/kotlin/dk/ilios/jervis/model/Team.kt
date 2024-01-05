package dk.ilios.jervis.model

import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.SpecialRules
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.properties.Delegates

class TeamHalfData(private val game: Game) {
    var totalRerolls: Int = 0
    var usedRerolls: Int = 0

}

class TeamDriveData(private val game: Game) {
    // Team related data
}

class TeamTurnData(private val game: Game) {
    var currentTurn by Delegates.observable(0u) { prop, old, new ->
        game.gameFlow.tryEmit(game)
    }
    var moveActions: Int
        get() = availableActions[PlayerActionType.MOVE]!!
        set(value) { availableActions[PlayerActionType.MOVE] = value }
    var passActions: Int
        get() = availableActions[PlayerActionType.PASS]!!
        set(value) { availableActions[PlayerActionType.PASS] = value }
    var handOffActions: Int
        get() = availableActions[PlayerActionType.HAND_OFF]!!
        set(value) { availableActions[PlayerActionType.HAND_OFF] = value }
    var blockActions: Int
        get() = availableActions[PlayerActionType.BLOCK]!!
        set(value) { availableActions[PlayerActionType.BLOCK] = value }
    var blitzActions: Int
        get() = availableActions[PlayerActionType.BLITZ]!!
        set(value) { availableActions[PlayerActionType.BLITZ] = value }
    var foulActions: Int
        get() = availableActions[PlayerActionType.FOUL]!!
        set(value) { availableActions[PlayerActionType.FOUL] = value }
    val availableActions = mutableMapOf(
        PlayerActionType.MOVE to 0,
        PlayerActionType.PASS to 0,
        PlayerActionType.HAND_OFF to 0,
        PlayerActionType.BLOCK to 0,
        PlayerActionType.BLITZ to 0,
        PlayerActionType.FOUL to 0,
    )
}

class TeamTemporaryData(private val game: Game) {
    val dieRoll = mutableListOf<DieResult>()
}

class Team(name: String, roster: Roster, coach: Coach): Collection<Player> {

    val noToPlayer = mutableMapOf<PlayerNo, Player>()

    // Fixed Team data, identifying the team
    val id: String = ""
    val name: String = name
    val coach: Coach = coach
    val roster: Roster = roster

//    val race: String
//    val race: String
    // Variable team data that might change during the game
    var reRolls: Int = 0
    var apothecaries: Int = 0
    var cheerLeaders: Int = 0
    var assistentCoaches: Int = 0
    var fanFactor: Int = 0
    var teamValue: Int = 0
    var treasury: Int = 0
    var dedicatedFans: Int = 0
    val specialRules: MutableList<SpecialRules> = mutableListOf()

    // Special team state that needs to be tracked for the given period
    lateinit var game: Game
    lateinit var halfData: TeamHalfData
    lateinit var driveData: TeamDriveData
    lateinit var turnData: TeamTurnData
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
    override val size: Int = noToPlayer.size
    override fun isEmpty(): Boolean = noToPlayer.isEmpty()
    override fun iterator(): Iterator<Player> = noToPlayer.values.iterator()
    override fun containsAll(elements: Collection<Player>): Boolean = noToPlayer.values.containsAll(elements)
    override fun contains(element: Player): Boolean = noToPlayer.containsValue(element)

    fun notifyDogoutChange() {
        val playersInDogout = noToPlayer.values.filter { it.location == DogOut }
        _dogoutState.tryEmit(playersInDogout)
    }
    private val _dogoutState = MutableSharedFlow<List<Player>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val dogoutFlow: SharedFlow<List<Player>> = _dogoutState

}
