package dk.ilios.jervis.controller

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.EnterProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.fsm.ProcedureStack
import dk.ilios.jervis.fsm.ProcedureState
import dk.ilios.jervis.logs.LogEntry
import dk.ilios.jervis.logs.SimpleLogEntry
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.FullGame
import dk.ilios.jervis.rules.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

sealed interface ListEvent
data class AddEntry(val log: LogEntry): ListEvent
data class RemoveEntry(val log: LogEntry): ListEvent

class GameController(
    rules: Rules,
    state: Game,
    val actionProvider: (controller: GameController, availableActions: List<ActionDescriptor>) -> Action,
) {
    private val _logsEvents: MutableSharedFlow<ListEvent> = MutableSharedFlow(replay = 0, extraBufferCapacity = 10_000)
    val logsEvents: Flow<ListEvent> = _logsEvents
    val logs: MutableList<LogEntry> = mutableListOf()
    val rules: Rules = rules
    val stack: ProcedureStack = ProcedureStack()
    val commands: MutableList<Command> = mutableListOf()
    val state: Game = state
    private var isStarted: Boolean = false
    private var replayMode: Boolean = false
    private var replayIndex: Int = -1
    private val isStopped = false

    private fun processNode(currentNode: Node) {
        when(currentNode) {
            is ComputationNode -> {
                // Reduce noise from Continue events
                val command = currentNode.applyAction(Continue, state, rules)
                commands.add(command)
                command.execute(state, this)
            }
            is ActionNode -> {
                val actions = currentNode.getAvailableActions(state, rules)
                val reportAvailableActions = SimpleLogEntry( "Available actions: ${actions.joinToString()}")
                commands.add(reportAvailableActions)
                reportAvailableActions.execute(state, this)
                val selectedAction = actionProvider(this@GameController, actions)
                val reportSelectedAction = SimpleLogEntry("Selected action: $selectedAction")
                commands.add(reportSelectedAction)
                reportSelectedAction.execute(state, this)
                val command = currentNode.applyAction(selectedAction, state, rules)
                commands.add(command)
                command.execute(state, this)
            }
            is ParentNode -> {
                val commands = when(stack.firstOrNull()!!.currentParentNodeState()) {
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

    fun start() {
        if (replayMode) {
            throw IllegalStateException("Replay mode is enabled")
        }
        if (isStarted) {
            throw IllegalStateException("Game was already started")
        }
        isStarted = true
        setInitialProcedure(FullGame)
        while (!stack.isEmpty() && !isStopped) {
            val currentState: Node = stack.currentNode()
            processNode(currentState)
        }
    }

    private fun setInitialProcedure(procedure: Procedure) {
        val command = compositeCommandOf(
            SimpleLogEntry("Set initial procedure: ${procedure.name()}[${procedure.initialNode.name()}]"),
            EnterProcedure(procedure)
        )
        commands.add(command)
        command.execute(state, this)
    }

    fun addLog(entry: LogEntry) {
        logs.add(entry)
        if (!_logsEvents.tryEmit(AddEntry(entry))) {
            TODO()
        }
    }

    fun removeLog(entry: LogEntry) {
        if (logs.lastOrNull() == entry) {
            _logsEvents.tryEmit(RemoveEntry(logs.removeLast()))
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

    fun currentProcedure(): ProcedureState? = stack.firstOrNull()

    fun addNode(nextState: Node) {
        stack.addNode(nextState)
    }

    fun removeNode() {
        stack.removeNode()
    }

    fun enableReplayMode() {
        this.replayMode = true
        this.replayIndex = commands.size
    }

    fun disableReplayMode() {
        checkReplayMode()
        while(forward()) { }
        this.replayMode = false
        this.replayIndex = -1
    }

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
}