package dk.ilios.jervis.model

import dk.ilios.jervis.model.context.ContextHolder
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.actions.pass.PassingInteferenceContext
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.RerollSourceId
import dk.ilios.jervis.rules.tables.Weather
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Transient
import kotlin.properties.Delegates

/**
 * Entry point for tracking the state of a game of Blood Bowl.
 * It should only contain the static state and not enforce any rules.
 *
 * All rules should either be enforced by a [dk.ilios.jervis.fsm.Procedure]
 * or by calling methods in [dk.ilios.jervis.rules.Rules]
 */
class Game(
    val homeTeam: Team,
    val awayTeam: Team,
    val field: Field
) {
    init {
        // Setup circular references, making it easier to navigate
        // the object graph.
        homeTeam.setGameReference(this)
        awayTeam.setGameReference(this)
    }

    companion object

    // Weather conditions for the field
    var weather: Weather = Weather.PERFECT_CONDITIONS

    // Game progress
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
        gameFlow.safeTryEmit(this)
    }

    // Global state properties
    // We should only have properties here that are relevant to more than
    // one procedure, otherwise it should be moved into a [ProcedureContext]
    fun isTurnOver(): Boolean = turnOver != null
    var turnOver: TurnOver? = null
    var homeGoals: Int = 0
    var homeExtraTimeGoals: Int = 0
    var homeSuddenDeathGoals: Int = 0
    var awayGoals: Int = 0
    var awayExtraTimeGoals: Int = 0
    var awaySuddenDeathGoals: Int = 0

    val homeScore: Int get() = homeGoals + homeExtraTimeGoals + homeSuddenDeathGoals
    val awayScore: Int get() = awayGoals + awayExtraTimeGoals + awaySuddenDeathGoals

    /**
     * The player that is being activated. This is set as soon as the player is
     * selected. Whether the player counts as being activated is determined by
     * [Player.available]
     */
    var activePlayer: Player? = null
    var kickingPlayer: Player? = null // TODO Move into a context?

    // Kick-off events are not considered any teams turn, which means
    // a number of rules are not applicable there.
    // Especially the concept of "Active Team", which would be neither.
    // But due to how many times we want to access the active team, we
    // are instead making a special note of whether it being kick-off
    // or not. If you ask for the active or inactive team during that
    // period, an exception is thrown.
    var isDuringKickOff: Boolean = false
    var canUseTeamRerolls: Boolean = false

    // Active/Inactive indicates a teams "active turn".
    // See page 42 in the rulebook.
    var activeTeam: Team = this.homeTeam
        get() {
            if (isDuringKickOff) INVALID_GAME_STATE("Active team does not exists during Kick-off.")
            return field
        }


    var inactiveTeam: Team = this.awayTeam
        get() {
            if (isDuringKickOff) INVALID_GAME_STATE("Inactive team does not exists during Kick-off.")
            return field
        }

    // Kicking/Receiving team is decided during the pre-game sequence.
    // See page 38 in the rulebook.
    var kickingTeam: Team = this.homeTeam
    var receivingTeam: Team = this.awayTeam
    var kickingTeamInLastHalf: Team = kickingTeam

    // Temporary states - Figure out where/how to store these
    var moveStepTarget: Pair<FieldCoordinate, FieldCoordinate>? = null

    // Context objects are state holders used by procedures
    // when they need to track state between nodes
    val contexts: ContextHolder = ContextHolder()
    var passingInteferenceContext: PassingInteferenceContext? = null
    var rerollContext: UseRerollContext? = null

    val balls: MutableList<Ball> = mutableListOf(Ball())
    // Easy reference to the ball that is currently being "handled" somehow.
    var currentBallReference: Ball? = null
    // Helper method for returning the current ball. Will throw an exception if no
    // ball was set as current.
    fun currentBall(): Ball {
        return currentBallReference ?: INVALID_GAME_STATE("No current ball found")
    }
    fun currentBallOrNull(): Ball? {
        return currentBallReference
    }

    /**
     * Returns a reference to the only ball on the field.
     * Will throw if multiple balls exits.
     */
    fun getBall(): Ball {
        return balls.single()
    }

    /**
     * Returns a reference to the current ball.
     * This method only works if one ball exists, otherwise an
     * exception is thrown.
     */
    fun singleBall(): Ball {
        if (balls.size > 1) {
            INVALID_GAME_STATE("More than one ball found")
        }
        return balls.first()
    }

    /**
     * Returns the player matching the given [PlayerId].
     * If no player matches, an [dk.ilios.jervis.utils.InvalidGameStateException] is thrown
     */
    fun getPlayerById(id: PlayerId): Player {
        return homeTeam.firstOrNull { it.id == id } ?: awayTeam.firstOrNull { it.id == id } ?: INVALID_GAME_STATE("Player with $id not found")
    }

    fun getRerollSourceById(id: RerollSourceId): RerollSource {
        // Optimize this
        return homeTeam.rerolls.firstOrNull { it.id == id }
            ?: homeTeam.flatMap { it.skills.filterIsInstance<RerollSource>() }.firstOrNull { skill-> skill.id == id }
            ?: awayTeam.rerolls.firstOrNull { it.id == id }
            ?: awayTeam.flatMap { it.skills.filterIsInstance<RerollSource>() }.firstOrNull { skill-> skill.id == id }
            ?: INVALID_GAME_STATE("Reroll $id could not be found")
    }

    fun notifyUpdate() {
        gameFlow.safeTryEmit(this)
    }

    @Transient
    val gameFlow = MutableSharedFlow<Game>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
