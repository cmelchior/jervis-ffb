package com.jervisffb.engine

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.CalculatedAction
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
import com.jervisffb.engine.model.Team
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

class ActionsRequest(
    val team: Team?,
    val actions: List<ActionDescriptor>
) {
    val size = actions.size
}

sealed interface ListEvent
data class AddEntry(val log: LogEntry) : ListEvent
data class RemoveEntry(val log: LogEntry) : ListEvent

data class GameDelta(
    val commands: List<Command>
)

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
        MANUAL, CALLBACK, TEST, NOT_STARTED
    }

    // Copy of Home and Away teams state, taken just before starting the game.
    // This is required so we can write the initial state to a save file (which
    // is required as we apply all commands in the save file to this state).
    var initialHomeTeamState: JsonElement? = null
    var initialAwayTeamState: JsonElement? = null

    val logsEvents: Flow<ListEvent> = state.logChanges
    val diceRollGenerator = diceGenerator
    val rules: Rules = rules
    val actionHistory: MutableList<GameAction> = mutableListOf() // List all actions provided by the user.
    val commands: MutableList<Command> = mutableListOf()
    val state: Game = state
    val stack: ProcedureStack = state.stack // Shortcut for accessing the stack
    private var actionMode = ActionMode.NOT_STARTED
    private var isStarted: Boolean = false
    private var replayMode: Boolean = false
    private var replayIndex: Int = -1
    private val isStopped = false
    // Track if last action is UNDO, because we want to disable all automatic actions in that case,
    // Not sure if this is the best way to do that. I guess we could extend the GameAction interface
    // with a `source` hint, but is that overkill?
    var lastActionWasUndo = false

    /**
     * Process current node when in Callback mode. i.e
     */
    private suspend fun processNode(
        currentNode: Node,
        actionProvider: suspend (controller: GameController, availableActions: ActionsRequest) -> GameAction,
    ) {
        when (currentNode) {
            is ComputationNode -> {
                // Reduce noise from Continue events
                val command = currentNode.applyAction(Continue, state, rules)
                commands.add(command)
                command.execute(state)
            }

            is ActionNode -> {
                // TODO This logic breaks when reverting state. Figure out a solution
                val request = ActionsRequest(
                    currentNode.actionOwner(state, rules),
                    currentNode.getAvailableActions(state, rules)
                )

                // If an action node just accept a single Continue event, it means that it is
                // taking a shortcut through some nodes. In that case, just apply it immediately
                // without notifying the user.
                val selectedAction = if (request.actions.size == 1 && request.actions.first() == ContinueWhenReady) {
                    Continue
                } else {
                    val reportAvailableActions = ReportAvailableActions(request)
                    commands.add(reportAvailableActions)
                    reportAvailableActions.execute(state)
                    actionProvider(this@GameController, request)
                }

                if (selectedAction != Undo) {
                    lastActionWasUndo = false
                    val reportSelectedAction = ReportHandleAction(selectedAction)
                    commands.add(reportSelectedAction)
                    reportSelectedAction.execute(state)
                    val command = currentNode.applyAction(selectedAction, state, rules)
                    commands.add(command)
                    command.execute(state)
                    state.notifyUpdate()
                } else {
                    lastActionWasUndo = true
                    undoLastAction()
                }
            }

            is ParentNode -> {
                val commands =
                    when (stack.peepOrNull()!!.getParentNodeState()) {
                        ParentNode.State.ENTERING -> currentNode.enterNode(state, rules)
                        ParentNode.State.RUNNING -> currentNode.runNode(state, rules)
                        ParentNode.State.EXITING -> currentNode.exitNode(state, rules)
                    }
                this.commands.add(commands)
                commands.execute(state)
            }

            else -> {
                throw IllegalStateException("Unsupported type: ${currentNode::class.simpleName}")
            }
        }
    }

    fun getAvailableActions(): ActionsRequest {
        if (stack.isEmpty()) return ActionsRequest(null, emptyList())
        if (stack.currentNode() !is ActionNode || stack.currentNode() is ComputationNode) {
            throw IllegalStateException("State machine is not waiting at an ActionNode: ${stack.currentNode()}")
        }
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        val actions = currentNode.getAvailableActions(state, rules)
        return ActionsRequest(currentNode.actionOwner(state, rules), actions)
    }


    fun processAction(action: GameAction) {
        if (actionMode != ActionMode.MANUAL && actionMode != ActionMode.TEST) {
            error("Invalid action mode: $actionMode. Must be ActionMode.MANUAL or ActionMode.TEST.")
        }
        when (action) {
            is CompositeGameAction -> action.list.forEach { processSingleAction(it) }
            else -> processSingleAction(action)
        }

        // Report actions available to the node we moved to after processing the action.
        val reportAvailableActions = ReportAvailableActions(getAvailableActions())
        commands.add(reportAvailableActions)
        reportAvailableActions.execute(state)
    }

    private fun processSingleAction(userAction: GameAction) {
        if (userAction != Undo) {
            lastActionWasUndo = false
            actionHistory.add(userAction)
            val reportSelectedAction = ReportHandleAction(userAction)
            commands.add(reportSelectedAction)
            reportSelectedAction.execute(state)
            val currentNode: ActionNode = stack.currentNode() as ActionNode
            val command = currentNode.applyAction(userAction, state, rules)
            commands.add(command)
            command.execute(state)
            rollForwardToNextActionNode()
        } else {
            lastActionWasUndo = true
            undoLastAction()
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
     */
    fun getDelta(): GameDelta {
        return GameDelta(commands)
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

    // Move the state machine forward until we get to the next ActionNode that requires
    // user input.
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
                    commands.add(command)
                    command.execute(state)
                }
                is ActionNode -> {
                    val command = currentNode.applyAction(Continue, state, rules)
                    commands.add(command)
                    command.execute(state)
                }
                is ParentNode -> {
                    val commands =
                        when (stack.peepOrNull()!!.getParentNodeState()) {
                            ParentNode.State.ENTERING -> currentNode.enterNode(state, rules)
                            ParentNode.State.RUNNING -> currentNode.runNode(state, rules)
                            ParentNode.State.EXITING -> currentNode.exitNode(state, rules)
                        }
                    this.commands.add(commands)
                    commands.execute(state)
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

    suspend fun startCallbackMode(actionProvider: suspend (controller: GameController, availableActions: ActionsRequest) -> GameAction) {
        if (actionMode != ActionMode.NOT_STARTED) {
            error("Controller already started: $actionMode")
        }
        actionMode = ActionMode.CALLBACK
        val backupActionProvider: suspend (GameController, ActionsRequest) -> GameAction = { controller: GameController, request: ActionsRequest ->
            actionProvider(controller, request).also { selectedAction ->
                if (selectedAction != Undo) {
                    actionHistory.add(selectedAction)
                }
            }
        }
        setupInitialStartingState()
        while (!stack.isEmpty()) {
            processNode(stack.currentNode(), backupActionProvider)
        }
    }

    private fun setInitialProcedure(procedure: Procedure) {
        val command =
            compositeCommandOf(
                SimpleLogEntry("Set initial procedure: ${procedure.name()}[${procedure.initialNode.name()}]", LogCategory.STATE_MACHINE),
                EnterProcedure(procedure),
            )
        commands.add(command)
        command.execute(state)
    }

    fun currentProcedure(): ProcedureState? = stack.peepOrNull()

    fun currentNode(): Node? = currentProcedure()?.currentNode()

    fun enableReplayMode() {
        this.replayMode = true
        this.replayIndex = commands.size
    }

    fun disableReplayMode() {
        checkReplayMode()
        while (forward()) { }
        this.replayMode = false
        this.replayIndex = -1
    }

    // Revert last action
    // Will only revert back until
    fun undoLastAction() {
        if (replayMode) {
            throw IllegalStateException(
                "Controller is in replay mode. `revert` is only available in manual mode.",
            )
        }
        if (actionHistory.isEmpty()) return
        // User actions are always prefixed with a `ReportHandleAction` command
        // So the way to revert a user action is to remove commands from the command
        // list until we see an action of that type. Once we do that, we can remove
        // the ReportHandleAction and then finally the action.

        // Remove initial logs for the current node that is waiting for input.
        while (commands.last() is ReportHandleAction || commands.last() is ReportAvailableActions) {
            commands.removeLast().undo(state)
        }

        // Now revert all commands from the last action
        while (commands.last() !is ReportHandleAction)  {
            val undoCommand = commands.removeLast()
            undoCommand.undo(state)
        }

        // Then remove the logs describing entering that node
        while (commands.last() is ReportHandleAction || commands.last() is ReportAvailableActions) {
            commands.removeLast().undo(state)
        }

        // Finally, remove the entry from the action history
        actionHistory.removeLast()
        state.notifyUpdate()
    }

    // Go backwards in the command history
    fun back(): Boolean {
        checkReplayMode()
        if (replayIndex == 0) {
            return false
        }
        replayIndex -= 1
        val undoCommand = commands[replayIndex]
        undoCommand.undo(state)
        return true
    }

    private inline fun checkReplayMode() {
        if (!replayMode) {
            throw IllegalStateException("Controller is not in replay mode.")
        }
    }

    fun forward(): Boolean {
        checkReplayMode()
        if (replayIndex == commands.size) {
            return false
        }
        commands[replayIndex].execute(state)
        replayIndex += 1
        return true
    }

    /**
     * Test method. Used to apply multiple [GameAction]s in on go.
     */
    fun rollForward(vararg actions: GameAction?) {
        if (actionMode != ActionMode.TEST) {
            error("Invalid action mode: $actionMode. Must be ActionMode.TEST.")
        }
        actions.forEach {
            if (it != null) {
                val action = if (it is CalculatedAction) it.get(state, rules) else it
                processAction(action)
            }
        }
    }
}
