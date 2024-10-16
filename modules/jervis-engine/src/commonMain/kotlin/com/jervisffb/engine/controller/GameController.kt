package com.jervisffb.engine.controller

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.EnterProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.ProcedureStack
import com.jervisffb.engine.fsm.ProcedureState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.ReportAvailableActions
import com.jervisffb.engine.reports.ReportHandleAction
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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

class GameController(
    rules: Rules,
    state: Game,
    diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()
) {
    // Copy of the state Home and Away teams, taken just before starting the game
    var initialHomeTeamState: JsonElement? = null
    var initialAwayTeamState: JsonElement? = null

    // TODO Figure out a better way to hook up the UI to the model, so we do not to create this buffer
    private val _logsEvents: MutableSharedFlow<ListEvent> = MutableSharedFlow(replay = 20_000)
    val logsEvents: Flow<ListEvent> = _logsEvents
    val logs: MutableList<LogEntry> = mutableListOf()
    val diceRollGenerator = diceGenerator
    val rules: Rules = rules
    val stack: ProcedureStack = ProcedureStack()
    val actionHistory: MutableList<GameAction> = mutableListOf() // List all actions provided by the user.
    val commands: MutableList<Command> = mutableListOf()
    val state: Game = state
    private var isStarted: Boolean = false
    private var replayMode: Boolean = false
    private var replayIndex: Int = -1
    private val isStopped = false
    // Track if last action is UNDO, because we want to disable all automatic actions in that case,
    // Not sure if this is the best way to do that. I guess we could extend the GameAction interface
    // with a `source` hint, but is that overkill?
    var lastActionWasUndo = false

    private suspend fun processNode(
        currentNode: Node,
        actionProvider: suspend (controller: GameController, availableActions: ActionsRequest) -> GameAction,
    ) {
        when (currentNode) {
            is ComputationNode -> {
                // Reduce noise from Continue events
                val command = currentNode.applyAction(Continue, state, rules)
                commands.add(command)
                command.execute(state, this)
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
                    val reportAvailableActions = ReportAvailableActions(request.actions)
                    commands.add(reportAvailableActions)
                    reportAvailableActions.execute(state, this)
                    actionProvider(this@GameController, request)
                }

                if (selectedAction != Undo) {
                    lastActionWasUndo = false
                    val reportSelectedAction = ReportHandleAction(selectedAction)
                    commands.add(reportSelectedAction)
                    reportSelectedAction.execute(state, this)
                    val command = currentNode.applyAction(selectedAction, state, rules)
                    commands.add(command)
                    command.execute(state, this)
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
                commands.execute(state, this)
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
        val reportAvailableActions = SimpleLogEntry("Available actions: ${actions.joinToString()}", LogCategory.STATE_MACHINE)
        commands.add(reportAvailableActions)
        reportAvailableActions.execute(state, this)
        return ActionsRequest(currentNode.actionOwner(state, rules), actions)
    }

    fun processAction(userAction: GameAction) {
        actionHistory.add(userAction)
        val reportSelectedAction = ReportHandleAction(userAction)
        commands.add(reportSelectedAction)
        reportSelectedAction.execute(state, this)
        val currentNode: ActionNode = stack.currentNode() as ActionNode
        val command = currentNode.applyAction(userAction, state, rules)
        commands.add(command)
        command.execute(state, this)
        rollForwardToNextActionNode()
    }

    fun startManualMode() {
        setupInitialStartingState()
        rollForwardToNextActionNode()
    }

    fun startTestMode(start: Procedure) {
        setupInitialStartingState(start)
        rollForwardToNextActionNode()

    }

    // TODO What does this do exactly
    private fun rollForwardToNextActionNode() {
        if (
            !stack.isEmpty() &&
            (
                stack.currentNode() is ComputationNode ||
                stack.currentNode() is ParentNode ||
                // Skip action nodes that only accept "Continue" events
                stack.currentNode() is ActionNode && getAvailableActions().let { it.actions.size == 1 && it.actions.first() == ContinueWhenReady }
            )
        ) {
            when (val currentNode: Node = stack.currentNode()) {
                is ComputationNode -> {
                    // Reduce noise from Continue events
                    val command = currentNode.applyAction(Continue, state, rules)
                    commands.add(command)
                    command.execute(state, this)
                }
                is ActionNode -> {
                    val command = currentNode.applyAction(Continue, state, rules)
                    commands.add(command)
                    command.execute(state, this)
                }
                is ParentNode -> {
                    val commands =
                        when (stack.peepOrNull()!!.getParentNodeState()) {
                            ParentNode.State.ENTERING -> currentNode.enterNode(state, rules)
                            ParentNode.State.RUNNING -> currentNode.runNode(state, rules)
                            ParentNode.State.EXITING -> currentNode.exitNode(state, rules)
                        }
                    this.commands.add(commands)
                    commands.execute(state, this)
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
        command.execute(state, this)
    }

    fun addLog(entry: LogEntry) {
        logs.add(entry)
        _logsEvents.safeTryEmit(AddEntry(entry))
    }

    fun removeLog(entry: LogEntry) {
        if (logs.lastOrNull() == entry) {
            val logEntry = logs.removeLast()
            _logsEvents.safeTryEmit(RemoveEntry(logEntry))
        } else {
            throw IllegalStateException("Log could not be removed: ${entry.message}")
        }
    }

    fun addProcedure(procedure: Procedure) {
        stack.pushProcedure(procedure)
    }

    fun addProcedure(procedure: ProcedureState) {
        stack.pushProcedure(procedure)
    }

    fun removeProcedure(): ProcedureState {
        return stack.popProcedure()
    }

    fun currentProcedure(): ProcedureState? = stack.peepOrNull()

    fun currentNode(): Node? = currentProcedure()?.currentNode()

    fun setCurrentNode(nextState: Node) {
        stack.peepOrNull()!!.setCurrentNode(nextState)
    }

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
            commands.removeLast().undo(state, this)
        }

        // Now revert all commands from the last action
        while (commands.last() !is ReportHandleAction)  {
            val undoCommand = commands.removeLast()
            undoCommand.undo(state, this)
        }

        // Then remove the logs describing entering that node
        while (commands.last() is ReportHandleAction || commands.last() is ReportAvailableActions) {
            commands.removeLast().undo(state, this)
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
        undoCommand.undo(state, this)
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
        commands[replayIndex].execute(state, this)
        replayIndex += 1
        return true
    }

    // Test method
    fun rollForward(vararg actions: GameAction?) {
        actions.forEach {
            if (it != null) {
                val action = if (it is CalculatedAction) it.get(state, rules) else it
                processAction(action)
                rollForwardToNextActionNode()
                gotoNextUserAction()
            }
        }
        gotoNextUserAction()
    }

    // Roll forward to next action that requires user input.
    // This means automatically providing "Continue" events if that
    // is the only option.
    private fun gotoNextUserAction() {
        var newActions = getAvailableActions()
        while (newActions.actions.size == 1 && newActions.actions.first() == ContinueWhenReady) {
            processAction(Continue)
            rollForwardToNextActionNode()
            newActions = getAvailableActions()
        }
    }
}
