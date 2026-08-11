package com.jervisffb.engine

import com.jervisffb.engine.actions.AddPlayerKeyword
import com.jervisffb.engine.actions.AddPlayerSkill
import com.jervisffb.engine.actions.AdminGameAction
import com.jervisffb.engine.actions.ChangePlayerBaseStat
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.MovePlayer
import com.jervisffb.engine.actions.RemovePlayerKeyword
import com.jervisffb.engine.actions.RemovePlayerSkill
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.SetBallState
import com.jervisffb.engine.actions.SetPlayerState
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.EnterProcedure
import com.jervisffb.engine.commands.ModifyPlayerBaseStat
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.MutableProcedureStack
import com.jervisffb.engine.fsm.MutableProcedureState
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.ReportAvailableActions
import com.jervisffb.engine.reports.ReportHandleAction
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.engine.utils.InvalidGameStateException
import com.jervisffb.engine.utils.isRandomAction
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.Flow

sealed interface ListEvent
data class AddEntry(val log: LogEntry) : ListEvent
data class RemoveEntry(val log: LogEntry) : ListEvent

/**
 * Main entry point for running a single game according to some predefined rules.
 **
 * This class should not be used until both teams have been identified and a ruleset
 * has been agreed upon. This should be the responsibility of a specific
 * [GameRunner]
 *
 * - [initialActions] Actions to run as soon as the controller is started using [startManualMode]
 *   or [startTestMode]
 *
 * - [validateActions] If `true`, all actions will be validated against [getAvailableActions]
 *   before [ActionNode.applyAction] is called. Illegal or invalid actions will throw an
 *   [InvalidActionException].
 *
 * - [cacheActionDescriptor] If `true`, the result of [getAvailableActions] will be cached
 *   and reused for subsequent calls to [getAvailableActions]. This can be useful if
 *   [getAvailableActions] is called frequently, but the result is not expected to change.
 *   It is most commonly `false` during unit tests as they are expected to modify the result.
 */
class GameEngineController(
    state: Game,
    private val initialActions: List<GameAction> = emptyList(),
    private val validateActions: Boolean = true,
    private val cacheActionDescriptor: Boolean = false,
    // If `true`, initial actions cannot be undone.
    private val protectInitialActions: Boolean = false,
    // Called when the controller is started.
    // If set, the controller will collect statistics about the game while it
    // is running.
    val statistics: GameStatistics? = null,
    private val onStarted: (GameEngineController) -> Unit = { controller -> /* Do nothing */ },
) {

    companion object {
        val LOG = jervisLogger()
    }

    // How is the GameController consuming actions. Once started, it poses
    // restrictions on the Controller is used.
    enum class ActionMode {
        MANUAL, TEST, NOT_STARTED
    }

    // Copy of Home and Away teams state, taken just before starting the game.
    // This is required so we can write the initial state to a save file (which
    // is required as we apply all commands in the save file to this state).
    var initialHomeTeamState: SerializedTeam? = null
    var initialAwayTeamState: SerializedTeam? = null

    // If `true`, a log entry will be created in `state.logs` for the next legally available actions.
    // This is nice for debugging, but it is a relatively costly operation, especially when selecting
    // pitch squares. So when running many games quickly, like during fuzz testing or AI training,
    // setting this to false can improve memory pressure and overall performance.
    var logAvailableActions: Boolean = true
    val logsEvents: Flow<ListEvent> = state.logChanges
    val rules: Rules = state.rules

    // Track the entire "forward" history. In case of Undo's. The last delta
    // is removed from history, reversed and put in `lastActionIfUndo`
    private val _history: MutableList<GameDelta> = mutableListOf()
    val history: List<GameDelta> = _history
    private val collectStatistics: Boolean = (statistics != null)

    // Point in `history` that calling Undo cannot move past.
    // Stays 0 unless `protectInitialActions` is set.
    private var undoFloor: Int = 0


    private var lastGameActionId: GameActionId = GameActionId(0)
    private var deltaBuilder = DeltaBuilder(lastGameActionId + 1, collectMetadata = collectStatistics)
    private var cachedActionRequest: ActionRequest? = null
    val state: Game = state
    val stack: MutableProcedureStack = state.stack // Shortcut for accessing the stack
    var actionMode = ActionMode.NOT_STARTED
    private var isStarted: Boolean = false
    private var replayMode: Boolean = false
    private var replayIndex: Int = -1
    private val isStopped = false

    // State for tracking Undo actions.
    var lastActionIfUndo: Pair<Node?, GameDelta>? = null
    fun lastActionWasUndo(): Boolean {
        return lastActionIfUndo != null
    }

    /**
     * If an error occurred during a call to [handleAction], it will also
     * be stored here before being thrown further out.
     */
    var lastHandleActionError: InvalidActionException? = null
        private set

    /**
     * Returns the last [GameAction] that was processed by [handleAction].
     */
    val lastAction: GameAction?
        get() {
            return if (lastActionIfUndo != null) {
                Undo
            } else {
                history.lastOrNull()?.steps?.lastOrNull()?.action
            }
        }

    /**
     * Returns a [ActionRequest] representing the available actions for the
     * current [Node] as well as who is responsible for providing it.
     */
    fun getAvailableActions(): ActionRequest {
        if (cacheActionDescriptor && cachedActionRequest?.id == currentActionIndex()) {
            return cachedActionRequest!!
        }
        if (stack.isEmpty()) return ActionRequest(nextActionIndex(), null, emptyList())
        if (stack.currentNode() !is ActionNode) {
            throw IllegalStateException("State machine is not waiting at an ActionNode: ${stack.currentNode()}")
        }
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        val actions = currentNode.getAvailableActions(state, rules)
        return ActionRequest(nextActionIndex(), currentNode.actionOwner(state, rules), actions).also {
            if (cacheActionDescriptor) {
                cachedActionRequest = it
            }
        }
    }

    /**
     * Process the given [action] as input to the currently active [Node].
     *
     * [getAvailableActions] will return a description of valid input to this
     * method. So if an invalid action is provided, a [com.jervisffb.engine.utils.InvalidActionException]
     * is thrown.
     */
    fun handleAction(action: GameAction) {
        LOG.i("[Game-${this.hashCode()}] Handling action (${currentActionIndex().value + 1}): $action")
        if (actionMode != ActionMode.MANUAL && actionMode != ActionMode.TEST) {
            error("Invalid action mode: $actionMode. Must be ActionMode.MANUAL or ActionMode.TEST.")
        }
        try {
            cachedActionRequest = null
            when (action) {
                is Undo -> {
                    if (isUndoAvailable(null)) {
                        undoLastAction(action, revertActionId = false)
                    } else {
                        INVALID_ACTION(Undo, "Undo is not available for the current game state or rule setup.")
                    }
                }
                is Revert -> {
                    // If Revert is sent, we assume the user knows what they are doing
                    // and just apply it without restrictions. This also includes
                    // ignoring `undoFloor`.
                    undoLastAction(action, revertActionId = true)
                }
                else -> processForwardAction(action)
            }
        } catch (ex: InvalidActionException) {
            lastHandleActionError = ex
            throw ex
        }
        LOG.d { "Current node: ${stack.stateToPrettyString()}" }
    }

    /**
     * Returns the delta from the last [GameAction] that was processed.
     *
     * This includes, the command processed and the resulting log statements
     * and [Command] updates that happened because of it.
     *
     * This allows a consumer better insights into what changed and make
     * it possible to keep shadow data structures updated in a more granular way,
     * rather than doing a full copy.
     *
     * If the last action was [Undo], this will return the reversed [GameDelta]
     * of the action that was undone.
     */
    fun getDelta(): GameDelta {
        return lastActionIfUndo?.second ?: _history.lastOrNull() ?: GameDelta(id = GameActionId(0), steps = emptyList())
    }

    /**
     * Start the GameController in manual mode. This mode requires consumers
     * to manually drive the rule engine. A simple example looks like this:
     *
     * ```
     * while (!controller.stack.isEmpty()) {
     *   val request = controller.getAvailableActions()
     *   val action = createAction(request)
     *   controller.handleAction(action)
     * }
     * ```
     *
     * @param logAvailableActions If `true`, the available actions after processing
     * a user action will be logged as a log entry in [Game.logs].
     */
    fun startManualMode(logAvailableActions: Boolean = true) {
        if (actionMode != ActionMode.NOT_STARTED) {
            error("Controller already started: $actionMode")
        }
        this.logAvailableActions = logAvailableActions
        actionMode = ActionMode.MANUAL
        setupInitialStartingState()
        rollForwardToNextActionNode()
        state.collectChanceData = (collectStatistics && statistics?.ignoreInitialActions != true)
        initialActions.forEach {
            handleAction(it)
        }
        lockInitialActions()
        state.collectChanceData = collectStatistics
        onStarted(this)
    }

    fun startTestMode(start: Procedure, logAvailableActions: Boolean = true) {
        actionMode = ActionMode.TEST
        this.logAvailableActions = logAvailableActions
        setupInitialStartingState(start)
        rollForwardToNextActionNode()
        initialActions.forEach {
            handleAction(it)
        }
        lockInitialActions()
        state.collectChanceData = collectStatistics
        onStarted(this)
    }

    /**
     * If [protectInitialActions] was set. This will mark the point in history
     * from where [Undo] or [Revert] no longer has any effect.
     */
    private fun lockInitialActions() {
        if (!protectInitialActions) return
        // Measured from history because a CompositeGameAction is split into
        // one delta per step.
        undoFloor = _history.size
    }

    fun currentProcedure(): MutableProcedureState? = stack.peepOrNull()

    fun currentNode(): Node? = currentProcedure()?.currentNode()

    fun previousNode(): Node? {
        return if (lastActionWasUndo()) {
            lastActionIfUndo?.first
        } else {
            history.lastOrNull()?.steps?.lastOrNull()?.node
        }
    }

    /**
     * Returns `true` if it is possible to [Undo] the current game state, `false` if not.
     *
     * @param team The team wanting to Undo. If set, it is only possible to undo if the
     * given team also created the game action.
     */
    fun isUndoAvailable(team: TeamId? = null): Boolean {
        if (_history.size <= undoFloor) return false
        if (rules.undoActionBehavior == UndoActionBehavior.NOT_ALLOWED) return false

        // Since CompositeGameActions are split into separate GameDeltas, it should be safe
        // to just check the first action (Technically that is not the case, but we should
        // never generate "random" actions in the middle of a Composite Action. These should
        // always be "normal" actions.
        if (
            rules.undoActionBehavior == UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS
            && history.last().steps.first().action.isRandomAction()
        ) {
            return false
        }

        // If a teamId is provided, only actions created by the same teamId can be
        // undone.
        if (team != null && history.last().owner != team) {
            return false
        }

        return true
    }

    /**
     * Returns the index of the last processed [GameAction]
     * I.e., this can be seen as a "version number" of the engine as game action indexes
     * are always incrementing, even when processing [Undo] actions.
     */
    fun currentActionIndex(): GameActionId {
        return lastGameActionId
    }

    fun nextActionIndex(): GameActionId {
        return lastGameActionId + 1
    }

    private fun undoLastAction(action: GameAction, revertActionId: Boolean) {
        if (replayMode) {
            throw IllegalStateException(
                "Controller is in replay mode. `revert` is only available in manual mode.",
            )
        }
        if (_history.isEmpty()) return
        val delta = _history.removeLast().reverse()
        lastActionIfUndo = currentNode() to delta
        delta.steps.forEach { step ->
            step.commands.forEach { command -> command.undo(state) }
        }

        // UNDO actions are "normal" commands that are synchronized across distributed clients, so will
        // also increment the action counter.
        // REVERT actions only happen on a single client, so instead decrement the counter, "hiding" the change.
        lastGameActionId = if (revertActionId) {
            (lastGameActionId - 1)
        } else {
            (lastGameActionId + 1)
        }
        statistics?.handleAction(action, delta)
    }

    private fun processForwardAction(userAction: GameAction) {
        lastActionIfUndo = null
        val actionOwner = currentNode().let { node ->
            if (node is ActionNode) {
                node.actionOwner(state, rules)?.id
            } else {
                null
            }
        }
        val newDeltaId = (lastGameActionId + 1)
        deltaBuilder = DeltaBuilder(newDeltaId, actionOwner, collectStatistics)
        when (userAction) {
            is Undo -> error("Invalid action: $userAction")
            is CompositeGameAction -> userAction.actionList.forEach { actionElement ->
                processSingleAction(deltaBuilder, actionElement)
            }
            else -> processSingleAction(deltaBuilder, userAction)
        }
        val delta = deltaBuilder.build()
        _history.add(delta)
        lastGameActionId = newDeltaId
        statistics?.handleAction(userAction, delta)
    }

    private fun processSingleAction(deltaBuilder: DeltaBuilder, userAction: GameAction) {
        val currentProcedure = stack.peepOrNull()!!
        deltaBuilder.beginAction(
            userAction,
            currentProcedure.procedure,
            currentProcedure.currentNode())
        logInternalEvent(ReportHandleAction(userAction))
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        if (validateActions && userAction !is AdminGameAction) {
            validateAction(userAction)
        }

        // If a procedure not a `ChanceObservationHandler` still requires a
        // chance event, we do not know how to interpret and can only report
        // it as an unstructured action. As it is not handle anywhere else, we
        // need to detect it here.
        val unstructuredChanceEvent = when {
            !state.collectChanceData -> null
            !userAction.isRandomAction() -> null
            // Classes implementing the `ChanceObservationHandler` interface promises
            // to add structured data themselves.
            currentProcedure.procedure is ChanceObservationHandler -> null
            else -> AddChanceObservation(
                ChanceObservation.UnstructuredAction(
                    index = state.nextAvailableChanceObservationIndex,
                    nodeDescription = stack.stateToPrettyString(),
                    actionName = userAction::class.simpleName ?: "UnknownGameAction",
                    dice = when (userAction) {
                        is DieResult -> listOf(userAction)
                        is DiceRollResults -> userAction.rolls
                        else -> emptyList()
                    },
                ),
            )
        }
        val actionCommand = when (userAction is AdminGameAction) {
            true -> processSingleDevAction(userAction)
            false -> currentNode.applyAction(userAction, state, rules)
        }

        val command = compositeCommandOf(actionCommand, unstructuredChanceEvent)
        executeCommand(command)
        rollForwardToNextActionNode()
        if (logAvailableActions) {
            logInternalEvent(ReportAvailableActions(getAvailableActions()))
        }
        deltaBuilder.endAction()
    }

    private fun processSingleDevAction(action: AdminGameAction): Command {
        if (!rules.allowPlayerEditsDuringGame) {
            INVALID_ACTION(action, "Player edits are not allowed during this game.")
        }
        val command = when (action) {
            is AddPlayerSkill -> {
                val player = action.getPlayer(state)
                val skill = rules.createSkill(player, action.skill, Duration.PERMANENT)
                com.jervisffb.engine.commands.AddPlayerSkill(player, skill)
            }
            is RemovePlayerSkill -> {
                val player = action.getPlayer(state)
                val skill = player.getSkill(action.skill.type)
                com.jervisffb.engine.commands.RemovePlayerSkill(player, skill)
            }
            is ChangePlayerBaseStat -> {
                val player = action.getPlayer(state)
                ModifyPlayerBaseStat(player, action.type, action.modifier)
            }
            is AddPlayerKeyword -> {
                val player = action.getPlayer(state)
                com.jervisffb.engine.commands.AddPlayerKeyword(player, action.keyword)
            }
            is RemovePlayerKeyword -> {
                val player = action.getPlayer(state)
                com.jervisffb.engine.commands.RemovePlayerKeyword(player, action.keyword)
            }
            is SetPlayerState -> {
                val player = action.getPlayer(state)
                val location = action.location
                val hasTackleZones = (action.state == PlayerPitchState.STANDING)
                if (location is OnPitchLocation) {
                    if (!location.isOnPitch(rules)) {
                        INVALID_ACTION(action, "Location is not on the pitch: $location")
                    }
                }
                compositeCommandOf(
                    com.jervisffb.engine.commands.SetPlayerState(player, action.state, hasTackleZones),
                    com.jervisffb.engine.commands.SetPlayerLocation(player, location)
                )
            }
            is MovePlayer -> {
                val player = action.getPlayer(state)
                if (player.state !is PlayerPitchState) {
                    INVALID_ACTION(action, "Player state is not compatible with being moved on the pitch: ${player.state}")
                }
                val location = action.location
                if (!location.isOnPitch(rules)) {
                    INVALID_ACTION(action, "Location is not a valid on-pitch location: $location")
                }
                when (location) {
                    is GiantLocation -> TODO("Not supported yet")
                    is PitchCoordinate -> {
                        val square = state.pitch[location]
                        if (square.isOccupied() || square.hasTrapdoor || square.hasChest || square.balls.isNotEmpty()) {
                            INVALID_ACTION(action, "Cannot move player to occupied square: $location")
                        }
                    }
                }
                com.jervisffb.engine.commands.SetPlayerLocation(player, location)
            }
            is SetBallState -> {
                val ball = action.getBall(state)
                val ballState = action.ballState
                val carrier = action.getPlayer(state)
                buildCompositeCommand {
                    add(com.jervisffb.engine.commands.SetBallLocation(ball, action.coordinate()))
                    if (ballState != null) {
                        add(when (ballState) {
                            BallState.ACCURATE_THROW -> com.jervisffb.engine.commands.SetBallState.accurateThrow(ball)
                            BallState.BOUNCING -> com.jervisffb.engine.commands.SetBallState.bouncing(ball)
                            BallState.CARRIED -> com.jervisffb.engine.commands.SetBallState.carried(ball, carrier!!)
                            BallState.DEVIATING -> com.jervisffb.engine.commands.SetBallState.deviating(ball)
                            BallState.IN_AIR -> com.jervisffb.engine.commands.SetBallState.inAir(ball)
                            BallState.ON_GROUND -> com.jervisffb.engine.commands.SetBallState.onGround(ball)
                            BallState.OUT_OF_BOUNDS -> com.jervisffb.engine.commands.SetBallState.outOfBounds(ball, ball.coordinates)
                            BallState.SCATTERED -> com.jervisffb.engine.commands.SetBallState.scattered(ball)
                            BallState.THROW_IN -> com.jervisffb.engine.commands.SetBallState.thrownIn(ball)
                            BallState.DEFLECTED -> com.jervisffb.engine.commands.SetBallState.deflected(ball)
                        })
                    }
                }
            }
        }
        return command
    }

    /**
     * Check if an action will be accepted by [ActionNode.applyAction]. If not
     * an [InvalidActionException] will be thrown.
     */
    private fun validateAction(action: GameAction) {
        if (!getAvailableActions().isValid(action)) {
            INVALID_ACTION(action, "Invalid action for ${stack.stateToPrettyString()}: $action")
        }
    }

    private fun executeCommand(command: Command) {
        deltaBuilder.addCommand(command)
        try {
            command.execute(state)
        } catch (ex: Exception) {
            val message = ex.message?.let {
                when (it.isBlank()) {
                    true -> "<no-message>"
                    false -> it
                }
            } ?: "<no-message>"
            throw InvalidGameStateException("${stack.stateToPrettyString()}: $message", ex)
        }
    }

    private fun logInternalEvent(log: LogEntry) {
        executeCommand(log)
    }

    // Move the state machine forward until we get to the next ActionNode that requires
    // user input.
    private fun rollForwardToNextActionNode() {
        if (
            !stack.isEmpty() &&
            (
                stack.currentNode() is ComputationNode ||
                    stack.currentNode() is ParentNode ||
                    // Some action nodes only accept "Continue" events if all other options have been exhausted.
                    // We want to roll over these as well.
                    (stack.currentNode() is ActionNode && getAvailableActions().singleOrNull() == ContinueWhenReady)
            )
        ) {
            val currentNode = stack.currentProcedure()?.currentNode()
            val currentProcedure = stack.currentProcedure()?.procedure
            when (currentNode) {
                is ComputationNode -> {
                    deltaBuilder.addIntermediateNode(currentProcedure, currentNode)
                    // Reduce noise from Continue events
                    val command = currentNode.applyAction(Continue, state, rules)
                    executeCommand(command)
                    rollForwardToNextActionNode()
                }
                is ActionNode -> {
                    deltaBuilder.addIntermediateNode(currentProcedure, currentNode)
                    val command = currentNode.applyAction(Continue, state, rules)
                    executeCommand(command)
                }
                is ParentNode -> {
                    val commands =
                        when (stack.peepOrNull()!!.getParentNodeState()) {
                            ParentNode.State.CHECK_SKIP -> {
                                deltaBuilder.addIntermediateNode(currentProcedure, currentNode)
                                currentNode.shouldEnterNode(state, rules)
                            }
                            ParentNode.State.ENTERING -> currentNode.enterNode(state, rules)
                            ParentNode.State.RUNNING -> currentNode.runNode(state, rules)
                            ParentNode.State.EXITING -> currentNode.exitNode(state, rules)
                        }
                    executeCommand(commands)
                }
                else -> {
                    throw IllegalStateException("Unsupported type: ${ if (currentNode != null) currentNode::class.simpleName else "null"}")
                }
            }
            rollForwardToNextActionNode()
        }
    }

    private fun setupInitialStartingState(startingProcedure: Procedure = rules.fullGameStep) {
        if (replayMode) {
            throw IllegalStateException("Replay mode is enabled")
        }
        if (isStarted) {
            throw IllegalStateException("Game was already started")
        }

        // Save a snapshot of the initial state for Home and Awway teams
        initialHomeTeamState = SerializedTeam.serialize(state.homeTeam)
        initialAwayTeamState = SerializedTeam.serialize(state.awayTeam)

        // Set up the initial starting procedure
        isStarted = true
        setInitialProcedure(startingProcedure)
    }

    private fun setInitialProcedure(procedure: Procedure) {
        val message = "Set initial procedure: ${procedure.stateToPrettyString(procedure.initialNode)}"
        LOG.v { "[Stack] $message" }
        val command =
            compositeCommandOf(
                SimpleLogEntry(message, LogCategory.STATE_MACHINE),
                EnterProcedure(procedure),
            )
        executeCommand(command)
    }
}
