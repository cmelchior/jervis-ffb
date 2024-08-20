package dk.ilios.jervis.model

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.procedures.CatchRollContext
import dk.ilios.jervis.procedures.CatchRollResultContext
import dk.ilios.jervis.procedures.PickupRollContext
import dk.ilios.jervis.procedures.PickupRollResultContext
import dk.ilios.jervis.procedures.RerollContext
import dk.ilios.jervis.procedures.RerollResultContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.BlockRollResultContext
import dk.ilios.jervis.procedures.actions.block.PushContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Transient
import kotlin.properties.Delegates

class Game(homeTeam: Team, awayTeam: Team, field: Field) {
    init {
        homeTeam.setGameReference(this)
        awayTeam.setGameReference(this)
    }

    companion object

    var isTurnOver = false
    var goalScored: Boolean = false
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0u) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }

    val homeTeam: Team = homeTeam
    val awayTeam: Team = awayTeam

    var activePlayer: Player? = null
    var kickingPlayer: Player? = null

    // Active/Inactive does indicate "active turn"
    var canUseTeamRerolls = false
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

    // How many different types of rolls are there that might be modified by skills etc
    // Go-For-It (Rushing)
    // Dodge
    // Stand Up
    // Pick Up Ball
    // Catch Ball
    // Hand-Off
    // Pass
    //
    // Block
    // Armor Roll
    // Injury Roll
    //

    var blockRollContext: BlockContext? = null
    var blockRollResultContext: BlockRollResultContext? = null
    var catchRollContext: CatchRollContext? = null
    var catchRollResultContext: CatchRollResultContext? = null
    var pickupRollContext: PickupRollContext? = null
    var pickupRollResultContext: PickupRollResultContext? = null
    var riskingInjuryRollsContext: RiskingInjuryRollContext? = null
    var pushContext: PushContext? = null

    var useRerollContext: RerollContext? = null
    var useRerollResult: RerollResultContext? = null

    val field: Field = field
    val ball: Ball = Ball()

    val ballSquare: FieldSquare
        get() = this.field[ball.location]

    fun getPlayerById(id: PlayerId): Player? {
        return homeTeam.firstOrNull { it.id == id } ?: awayTeam.firstOrNull { it.id == id }
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

    fun notifyUpdate() {
        gameFlow.safeTryEmit(this)
    }

    @Transient
    val gameFlow = MutableSharedFlow<Game>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
