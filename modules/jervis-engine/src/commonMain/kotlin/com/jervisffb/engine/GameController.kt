package com.jervisffb.engine

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
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
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.ReportAvailableActions
import com.jervisffb.engine.reports.ReportHandleAction
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.serialize.JervisSerialization
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

sealed interface ListEvent
data class AddEntry(val log: LogEntry) : ListEvent
data class RemoveEntry(val log: LogEntry) : ListEvent

/**
 * Main entry point for controlling a single game.
 *
 * This class should not be used until both teams have been identified and a ruleset
 * has been agreed upon.
 */
class GameController(
    rules: Rules,
    state: Game,
    diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()
) {

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
    val diceRollGenerator = diceGenerator
    val rules: Rules = rules

    // Track the entire "forward" history. In case of Undo's. The last delta
    // is removed from history, reversed and put in `lastActionIfUndo`
    private val _history: MutableList<GameDelta> = mutableListOf()
    val history: List<GameDelta> = _history
    private var deltaBuilder = DeltaBuilder(_history.size)
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
     * method. So if an invalid is provided, a [com.jervisffb.engine.utils.InvalidActionException]
     * is thrown.
     */
    fun processAction(action: GameAction) {
        if (actionMode != ActionMode.MANUAL && actionMode != ActionMode.TEST) {
            error("Invalid action mode: $actionMode. Must be ActionMode.MANUAL or ActionMode.TEST.")
        }
        if (action is Undo) {
            undoLastAction()
        } else {
            processForwardAction(action)
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
     * of the last action being processed.
     */
    fun getDelta(): GameDelta {
        return lastActionIfUndo ?: _history.lastOrNull() ?: GameDelta(0, emptyList())
    }

    /**
     * Start the GameController in manual mode. This mode requires consumers
     * to manually drive the rule engine. A simple example looks like this:
     *
     * ```
     * while (!controller.stack.isEmpty()) {
     *   val request = controller.getAvailableActions()
     *   val action = createAction(request)
     *   controller.processAction(action)
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
    }

    fun startTestMode(start: Procedure) {
        actionMode = ActionMode.TEST
        setupInitialStartingState(start)
        rollForwardToNextActionNode()
    }

    fun currentProcedure(): ProcedureState? = stack.peepOrNull()

    fun currentNode(): Node? = currentProcedure()?.currentNode()

    private fun undoLastAction() {
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
    }

    private fun processForwardAction(userAction: GameAction) {
        lastActionIfUndo = null
        deltaBuilder = DeltaBuilder(_history.size)
        when (userAction) {
            is Undo -> error("Invalid action: $userAction")
            is CompositeGameAction -> userAction.list.forEach { processSingleAction(it) }
            else -> processSingleAction(userAction)
        }
        val delta = deltaBuilder.build()
        _history.add(delta)
    }

    private fun processSingleAction(userAction: GameAction) {
        val currentProcedure = stack.peepOrNull()!!
        deltaBuilder.beginAction(userAction, currentProcedure.procedure, currentProcedure.currentNode())
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
                stack.currentNode() is ActionNode && getAvailableActions().let { it.actions.size == 1 && it.actions.first() == ContinueWhenReady }
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
