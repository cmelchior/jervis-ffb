package com.jervisffb.engine

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.EnterProcedure
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.ProcedureStack
import com.jervisffb.engine.fsm.ProcedureState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.ReportAvailableActions
import com.jervisffb.engine.reports.ReportHandleAction
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.isRandomAction
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

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
 * @param initialActions Actions to run as soon as the controller is started using [startManualMode]
 * or [startTestMode]
 */
class GameEngineController(state: Game, private val initialActions: List<GameAction> = emptyList()) {

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
    var initialHomeTeamState: JsonElement? = null
    var initialAwayTeamState: JsonElement? = null

    val logsEvents: Flow<ListEvent> = state.logChanges
    val rules: Rules = state.rules

    // Track the entire "forward" history. In case of Undo's. The last delta
    // is removed from history, reversed and put in `lastActionIfUndo`
    private val _history: MutableList<GameDelta> = mutableListOf()
    val history: List<GameDelta> = _history
    private var lastGameActionId: GameActionId = GameActionId(0)
    private var deltaBuilder = DeltaBuilder(lastGameActionId + 1)
    val state: Game = state
    val stack: ProcedureStack = state.stack // Shortcut for accessing the stack
    var actionMode = ActionMode.NOT_STARTED
    private var isStarted: Boolean = false
    private var replayMode: Boolean = false
    private var replayIndex: Int = -1
    private val isStopped = false

    // State for tracking Undo actions.
    var lastActionIfUndo: GameDelta? = null
    fun lastActionWasUndo(): Boolean {
        return lastActionIfUndo != null
    }

    /**
     * Returns a [ActionRequest] representing the available actions for the
     * current [Node] as well as who is responsible for providing it.
     */
    fun getAvailableActions(): ActionRequest {
        if (stack.isEmpty()) return ActionRequest(null, emptyList())
        if (stack.currentNode() !is ActionNode) {
            throw IllegalStateException("State machine is not waiting at an ActionNode: ${stack.currentNode()}")
        }
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        val actions = currentNode.getAvailableActions(state, rules)
        return ActionRequest(currentNode.actionOwner(state, rules), actions)
    }

    /**
     * Process the given [action] as input to the currently active [Node].
     *
     * [getAvailableActions] will return a description of valid input to this
     * method. So if an invalid action is provided, a [com.jervisffb.engine.utils.InvalidActionException]
     * is thrown.
     */
    fun handleAction(action: GameAction) {
        LOG.i("[Game-XX] Handling action (${currentActionIndex().value + 1}): $action")
        if (actionMode != ActionMode.MANUAL && actionMode != ActionMode.TEST) {
            error("Invalid action mode: $actionMode. Must be ActionMode.MANUAL or ActionMode.TEST.")
        }
        when (action) {
            is Undo -> {
                if (isUndoAvailable(null)) {
                    undoLastAction(revertActionId = false)
                } else {
                    throw INVALID_ACTION(Undo, "Undo is not available for the current game state or rule setup.")
                }
            }
            is Revert -> {
                // If Revert is sent, we assume the user knows what they are doing
                // and just apply it without restrictions.
                undoLastAction(revertActionId = true)
            }
            else -> processForwardAction(action)
        }
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
        return lastActionIfUndo ?: _history.lastOrNull() ?: GameDelta(id = GameActionId(0), steps = emptyList())
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
     */
    fun startManualMode() {
        if (actionMode != ActionMode.NOT_STARTED) {
            error("Controller already started: $actionMode")
        }
        actionMode = ActionMode.MANUAL
        setupInitialStartingState()
        rollForwardToNextActionNode()
        initialActions.forEach {
            handleAction(it)
        }
    }

    fun startTestMode(start: Procedure) {
        actionMode = ActionMode.TEST
        setupInitialStartingState(start)
        rollForwardToNextActionNode()
        initialActions.forEach {
            handleAction(it)
        }
    }

    fun currentProcedure(): ProcedureState? = stack.peepOrNull()

    fun currentNode(): Node? = currentProcedure()?.currentNode()

    /**
     * Returns `true` if it is possible to [Undo] the current game state, `false` if not.
     *
     * @param team The team wanting to Undo. If set, it is only possible to undo if the
     * given team also created the game action.
     */
    fun isUndoAvailable(team: TeamId? = null): Boolean {
        if (_history.isEmpty()) return false
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

    private fun undoLastAction(revertActionId: Boolean) {
        if (replayMode) {
            throw IllegalStateException(
                "Controller is in replay mode. `revert` is only available in manual mode.",
            )
        }
        if (_history.isEmpty()) return
        val delta = _history.removeLast().reverse()
        lastActionIfUndo = delta
        delta.steps.forEach { step ->
            step.commands.forEach { command -> command.undo(state) }
        }

        // UNDO actions are "normal" commands that is synchronized across distributed clients, so will
        // also increment the action counter.
        // REVERT actions only happen on a single client, so instead decrement the counter, "hiding" the change.
        lastGameActionId = if (revertActionId) {
            (lastGameActionId - 1)
        } else {
            (lastGameActionId + 1)
        }
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
        deltaBuilder = DeltaBuilder(newDeltaId, actionOwner)
        when (userAction) {
            is Undo -> error("Invalid action: $userAction")
            is CompositeGameAction -> userAction.list.forEach { actionElement ->
                processSingleAction(deltaBuilder, actionElement)
            }
            else -> processSingleAction(deltaBuilder, userAction)
        }
        val delta = deltaBuilder.build()
        _history.add(delta)
        lastGameActionId = newDeltaId
    }

    private fun processSingleAction(deltaBuilder: DeltaBuilder, userAction: GameAction) {
        val currentProcedure = stack.peepOrNull()!!
        deltaBuilder.beginAction(
            userAction,
            currentProcedure.procedure,
            currentProcedure.currentNode())
        logInternalEvent(ReportHandleAction(userAction))
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        val command = currentNode.applyAction(userAction, state, rules)
        executeCommand(command)
        rollForwardToNextActionNode()
        logInternalEvent(ReportAvailableActions(getAvailableActions()))
        deltaBuilder.endAction()
    }

    private fun executeCommand(command: Command) {
        deltaBuilder.addCommand(command)
        command.execute(state)
    }

    private fun logInternalEvent(log: LogEntry) {
        executeCommand(log)
    }

    // Move the state machine forward until we get to the next ActionNode that requires
    // user input. Only used in MANUAL and TEST mode.
    private fun rollForwardToNextActionNode() {
        if (
            !stack.isEmpty() &&
            (
                stack.currentNode() is ComputationNode ||
                    stack.currentNode() is ParentNode ||
                    // Some action nodes only accept "Continue" events if all other options have been exhausted
                    // We want to roll over these as well.
                    stack.currentNode() is ActionNode && getAvailableActions().let { it.size == 1 && it.first() == ContinueWhenReady }
            )
        ) {
            when (val currentNode: Node = stack.currentNode()) {
                is ComputationNode -> {
                    // Reduce noise from Continue events
                    val command = currentNode.applyAction(Continue, state, rules)
                    executeCommand(command)
                    rollForwardToNextActionNode()
                }
                is ActionNode -> {
                    val command = currentNode.applyAction(Continue, state, rules)
                    executeCommand(command)
                }
                is ParentNode -> {
                    val commands =
                        when (stack.peepOrNull()!!.getParentNodeState()) {
                            ParentNode.State.ENTERING -> currentNode.enterNode(state, rules)
                            ParentNode.State.RUNNING -> currentNode.runNode(state, rules)
                            ParentNode.State.EXITING -> currentNode.exitNode(state, rules)
                        }
                    executeCommand(commands)
                }
                else -> {
                    throw IllegalStateException("Unsupported type: ${currentNode::class.simpleName}")
                }
            }
            rollForwardToNextActionNode()
        }
    }

    private fun setupInitialStartingState(startingProcedure: Procedure = FullGame) {
        if (replayMode) {
            throw IllegalStateException("Replay mode is enabled")
        }
        if (isStarted) {
            throw IllegalStateException("Game was already started")
        }

        // Save a snapshot of the initial state for Home and Awway teams
        initialHomeTeamState = JervisSerialization.createTeamSnapshot(state.homeTeam)
        initialAwayTeamState = JervisSerialization.createTeamSnapshot(state.awayTeam)

        // Set up the initial starting procedure
        isStarted = true
        setInitialProcedure(startingProcedure)
    }

    private fun setInitialProcedure(procedure: Procedure) {
        val command =
            compositeCommandOf(
                SimpleLogEntry("Set initial procedure: ${procedure.name()}[${procedure.initialNode.name()}]", LogCategory.STATE_MACHINE),
                EnterProcedure(procedure),
            )
        executeCommand(command)
    }

    // TODO Figure out a better API for controlling Replay Mode.
//    fun enableReplayMode() {
//        this.replayMode = true
//        this.replayIndex = commands.size
//    }

//    fun disableReplayMode() {
//        checkReplayMode()
//        while (forward()) { }
//        this.replayMode = false
//        this.replayIndex = -1
//    }

//    // Go backwards in the command history
//    fun back(): Boolean {
//        checkReplayMode()
//        if (replayIndex == 0) {
//            return false
//        }
//        replayIndex -= 1
//        val undoCommand = commands[replayIndex]
//        undoCommand.undo(state)
//        return true
//    }

    private inline fun checkReplayMode() {
        if (!replayMode) {
            throw IllegalStateException("Controller is not in replay mode.")
        }
    }


//    fun forward(): Boolean {
//        checkReplayMode()
//        if (replayIndex == commands.size) {
//            return false
//        }
//        commands[replayIndex].execute(state)
//        replayIndex += 1
//        return true
//    }
}
