package dk.ilios.jervis.model

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.model.context.ContextHolder
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.procedures.DeviateRollContext
import dk.ilios.jervis.procedures.ScatterRollContext
import dk.ilios.jervis.procedures.ThrowInContext
import dk.ilios.jervis.procedures.actions.blitz.BlitzContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.BlockResultContext
import dk.ilios.jervis.procedures.actions.block.BothDownContext
import dk.ilios.jervis.procedures.actions.block.PushContext
import dk.ilios.jervis.procedures.actions.block.StumbleContext
import dk.ilios.jervis.procedures.actions.foul.FoulContext
import dk.ilios.jervis.procedures.actions.handoff.HandOffContext
import dk.ilios.jervis.procedures.actions.pass.PassingInteferenceContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.tables.Weather
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

    var weather: Weather = Weather.PERFECT_CONDITIONS

    var isTurnOver = false
    var goalScored: Boolean = false
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }

    val homeTeam: Team = homeTeam
    val awayTeam: Team = awayTeam

    var activePlayer: Player? = null
    var kickingPlayer: Player? = null

    // In some cases team rerolls are not allowed, like during setups
    var canUseTeamRerolls = false
    // Active/Inactive does indicate "active turn"
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

    // Context objects are state holders used by procedures
    // when they need to track state between nodes
    val contexts: ContextHolder = ContextHolder()
    var blockContext: BlockContext? = null
    var blockRollResultContext: BlockResultContext? = null
    var riskingInjuryRollsContext: RiskingInjuryRollContext? = null
    var pushContext: PushContext? = null
    var bothDownContext: BothDownContext? = null
    var stumbleContext: StumbleContext? = null
    var blitzContext: BlitzContext? = null
    var foulContext: FoulContext? = null
    var handOffContext: HandOffContext? = null
    var scatterRollContext: ScatterRollContext? = null
    var deviateRollContext: DeviateRollContext? = null
    var passingInteferenceContext: PassingInteferenceContext? = null
    var throwInContext: ThrowInContext? = null
    var rerollContext: UseRerollContext? = null

    val field: Field = field
    val ball: Ball = Ball()

    val ballSquare: FieldSquare
        get() {
            return ball.carriedBy?.let { player ->
                this.field[player.location.coordinate]
            } ?: this.field[ball.location]

        }
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
