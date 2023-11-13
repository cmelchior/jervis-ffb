package dk.ilios.bowlbot.model

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.properties.Delegates

data class Game(val p1: Player, val p2: Player) {
    var goalScored: Boolean = false
    var halfNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.tryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.tryEmit(this)
    }
    val homeTeam: Team = Team("HomeTeam", this)
    val awayTeam: Team = Team("AwayTeam", this)
    var currentTeam = homeTeam
        set(team) {
            otherTeam = currentTeam
            field = team
        }
    var otherTeam = awayTeam
        private set

    val gameFlow = MutableSharedFlow<Game>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
