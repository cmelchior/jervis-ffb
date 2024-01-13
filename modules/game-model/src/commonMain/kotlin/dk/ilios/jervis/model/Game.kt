package dk.ilios.jervis.model

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.PlayerAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.properties.Delegates

class Game(homeTeam: Team, awayTeam: Team, field: Field) {

    init {
        homeTeam.setGameReference(this)
        awayTeam.setGameReference(this)
    }

    companion object

    var goalScored: Boolean = false
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0u) { prop, old, new ->
        gameFlow.tryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.tryEmit(this)
    }

    val homeTeam: Team = homeTeam
    val awayTeam: Team = awayTeam

    var activePlayer: Player? = null
    var kickingPlayer: Player? = null

    var activeTeam: Team = this.homeTeam
    var inactiveTeam: Team = this.awayTeam
    var kickingTeam: Team = this.homeTeam
    var receivingTeam: Team = this.awayTeam
    var kickingTeamInLastHalf: Team = kickingTeam

    // Temporary states - Figure out where/how to store these
    var activePlayerAction: PlayerAction? = null
    var moveStepTarget: Pair<FieldCoordinate, FieldCoordinate>? = null
    var coinSideSelected: Coin? = null
    var coinResult: Coin? = null
    var pitchInvasionHomeRoll: D6Result? = null
    var pitchInvasionAwayRoll: D6Result? = null
    var pitchInvasionHomeResult: Int = 0
    var pitchInvasionAwayResult: Int = 0
    var pitchInvasionHomeTeamPlayersAffected: Int = 0
    var pitchInvasionAwayTeamPlayersAffected: Int = 0

    val field: Field = field
    val ball: Ball = Ball()

    fun getPlayerById(id: PlayerId): Player? {
        return homeTeam.firstOrNull { it.id == id } ?: awayTeam.firstOrNull { it.id == id}
    }

    fun swapKickingTeam() {
        val currentKickingTeam = kickingTeam
        kickingTeam = receivingTeam
        receivingTeam = currentKickingTeam
    }

    fun swapActiveTeam() {
        val currentTeam = activeTeam
        activeTeam = inactiveTeam
        inactiveTeam = currentTeam
    }

    val gameFlow = MutableSharedFlow<Game>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
