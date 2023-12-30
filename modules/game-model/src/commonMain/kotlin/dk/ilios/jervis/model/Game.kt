package dk.ilios.jervis.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.properties.Delegates

class Game(homeTeam: Team, awayTeam: Team, field: Field) {
    var goalScored: Boolean = false
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0u) { prop, old, new ->
        gameFlow.tryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.tryEmit(this)
    }

    val homeTeam = homeTeam
    val awayTeam = awayTeam

    var activePlayer: Player? = null
    var kickingPlayer: Player? = null

    var activeTeam: Team = this.homeTeam
    var inactiveTeam: Team = this.awayTeam
    var kickingTeam: Team = this.homeTeam
    var receivingTeam: Team = this.awayTeam
    var kickingTeamInLastHalf: Team = kickingTeam

    val field: Field = field
    val ball: Ball = Ball()

    init {
        homeTeam.setGameReference(this)
        awayTeam.setGameReference(this)
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
