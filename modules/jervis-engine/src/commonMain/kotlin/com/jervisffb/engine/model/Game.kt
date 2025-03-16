package com.jervisffb.engine.model

import com.jervisffb.engine.AddEntry
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.ListEvent
import com.jervisffb.engine.RemoveEntry
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.ProcedureStack
import com.jervisffb.engine.fsm.ProcedureState
import com.jervisffb.engine.model.context.ContextHolder
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassingInteferenceContext
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.safeTryEmit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.properties.Delegates

// TODO Just keep it as a singleton until we explore the requirements further.
/**
 * Deterministic ID generator used to generate IDs [Command] and other
 * objects created as part of [ActionNode.applyAction].
 *
 * Note, in particular, this class should _NOT_ be used as part of
 * [ActionNode.getAvailableActions] since we do not control when this is called,
 *
 * These ID's should only be generated from inside a request to
 * [GameEngineController.handleAction] ensuring that all access to the generator
 * is thread-safe. As it is not thread-safe by itself.
 *
 * The generator is tied to a specific [Game] instance. Ids are always incremental
 * even if actions are undone. This ensures that we all connected clients can
 * agree on the sequence of events even with [com.jervisffb.engine.actions.Undo] in
 * the mix.
 */
class IdGenerator {
    private data class Snapshot(
        val diceId: Int,
        val logId: Int,
    )

    private var diceId: Int = 0
    private var logId: Int = 0

    fun reset() {
        diceId = 0
        logId = 0
    }

    fun nextDiceId(): DieId {
        return DieId((++diceId).toString())
    }

    fun nextLogId(): String {
        return (++logId).toString()
    }
}

/**
 * Entry point for tracking the state of a game of Blood Bowl.
 * It should only contain the static state and not enforce any rules.
 *
 * All rules should either be enforced by a [com.jervisffb.fsm.Procedure]
 * or by calling methods in [com.jervisffb.rules.Rules]. This means that
 * changes to this class must only happen through [com.jervisffb.engine.commands]
 * objects.
 */
class Game(
    val rules: Rules,
    val homeTeam: Team,
    val awayTeam: Team,
    val field: Field,
) {
    init {
        // Setup circular references, making it easier to navigate
        // the object graph.
        homeTeam.setGameReference(this)
        awayTeam.setGameReference(this)
    }

    companion object

    val idGenerator = IdGenerator()

    // Track all current active procedures.
    val stack = ProcedureStack()

    // Track all logs related to the game state, this includes
    // game progress as well as meta data debug events like procedure
    // changes.
    val logs: MutableList<LogEntry> = mutableListOf()
    // TODO Figure out a better way to hook up the UI to the model, so we do not to create this buffer
    val logChanges: MutableSharedFlow<ListEvent> = MutableSharedFlow(replay = 20_000)

    // Weather conditions for the field
    var weather: Weather = Weather.PERFECT_CONDITIONS

    // Game progress
    var abortIfBallOutOfBounds: Boolean = false
    var halfNo by Delegates.observable(0) { prop, old, new ->
//        gameFlow.safeTryEmit(this)
    }
    var driveNo by Delegates.observable(0) { prop, old, new ->
//        gameFlow.safeTryEmit(this)
    }

    // Global state properties
    // We should only have properties here that are relevant to more than
    // one procedure, otherwise it should be moved into a [ProcedureContext]
    fun isTurnOver(): Boolean = turnOver != null

    // Checks if an action should end immediately.
    // It feels wrong to have this method here (since contains some logic and
    // reference a context). Should it be an utility method or be in the Rules
    // instead?
    fun endActionImmediately(): Boolean {
        return (
            isTurnOver() ||
                hasConceeded != null ||
                getContextOrNull<ActivatePlayerContext>()?.activationEndsImmediately == true
        )
    }

    var turnOver: TurnOver? = null
    var hasConceeded: Team? = null
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
     * If no player matches, an [com.jervisffb.utils.InvalidGameStateException] is thrown
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

//    @Transient
//    val gameFlow = MutableSharedFlow<Game>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun currentProcedure(): ProcedureState? = stack.peepOrNull()

    fun addProcedure(procedure: Procedure) {
        stack.pushProcedure(procedure)
    }

    fun addProcedure(procedure: ProcedureState) {
        stack.pushProcedure(procedure)
    }

    fun removeProcedure(): ProcedureState {
        return stack.popProcedure()
    }

    fun addLog(entry: LogEntry) {
        // Inject log id before exposing it to the outside.
        // Not the nicest, but quick to do while figuring out a better solution.
        entry.id = idGenerator.nextLogId()
        logs.add(entry)
        logChanges.safeTryEmit(AddEntry(entry))
    }

    fun removeLog(entry: LogEntry) {
        if (logs.lastOrNull() == entry) {
            val logEntry = logs.removeLast()
            logChanges.safeTryEmit(RemoveEntry(logEntry))
        } else {
            throw IllegalStateException("Log could not be removed: ${entry.message}")
        }
    }

    fun setCurrentNode(nextState: Node) {
        stack.peepOrNull()!!.setCurrentNode(nextState)
    }

}
