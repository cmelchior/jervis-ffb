package com.jervisffb.engine

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.CompositeCommand
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

/**
 * Class responsible for tracking all changes to the model that happened between
 * processing a users [GameAction] and it being ready to accept the next action.
 *
 * A single [GameAction] might trigger multiple steps that cross several nodes,
 * e.g., if it is a [CompositeGameAction] or if a [Continue] action is triggered
 * automatically.
 *
 * These are captured as individual [ActionStep].
 */
data class GameDelta(
    val id: Int,
    val steps: List<ActionStep>,
    val reversed: Boolean = false,
) {
    fun containsAction(action: GameAction): Boolean {
        return steps.any { it.action == action }
    }

    fun allCommands(): List<Command> {
        return steps.flatMap { it.commands }
    }

    fun containsCommand(predicate: (Command) -> Boolean): Boolean {
        return steps.any { it.commands.any(predicate) }
    }

    /**
     * Return a copy of this delta, but with all actions, commands reversed
     */
    fun reverse(): GameDelta {
        return GameDelta(
            id = id,
            steps = steps.reversed().map {
                it.copy(commands = it.commands.reversed())
            },
            reversed = true
        )
    }
}

/**
 * Class encapsulating changes to the model state happening due to a single
 * [GameAction]. It captures all changes from one user facing action to the
 * next. This means that e.g. [Continue] events that are applied internally are
 * considered part of the current Action, so all commands executed due to this
 * are considered part of the current [ActionStep]
 *
 * [CompositeGameAction]s will create an [ActionStep] for each action it
 * consists of.
 */
data class ActionStep(
    val action: GameAction,
    val procedure: Procedure,
    val node: Node,
    // Commands are flattened, i.e., a hierarchy of CompositeCommands is unrolled into a single long list.
    val commands: List<Command>,
) {
    val logs: List<LogEntry> = commands.filterIsInstance<LogEntry>()
    val gameProgress: List<LogEntry> = commands
        .filterIsInstance<LogEntry>()
        .filter { it.category == LogCategory.GAME_PROGRESS }
}

internal class DeltaBuilder(val deltaId: Int) {

    private val steps = mutableListOf<ActionStep>()

    private var currentAction: GameAction? = null
    private var currentProcedure: Procedure? = null
    private var currentNode: Node? = null
    private val commands: MutableList<Command> = mutableListOf()
    private val logs: MutableList<LogEntry> = mutableListOf()

    // For now treat everything between public actions as one step, even if it might involve multiple node
    // transitions
    fun beginAction(action: GameAction, procedure: Procedure, node: Node) {
        currentAction = action
        currentProcedure = procedure
        currentNode = node
        commands.clear()
    }

    fun addCommand(command: Command) {
        when (command) {
            is CompositeCommand -> command.commands.forEach { addCommand(it) }
//            is LogEntry -> logs.add(command)
            else -> commands.add(command)
        }
    }

    fun endAction() {
        val newStep = ActionStep(
            currentAction!!, currentProcedure!!, currentNode!!, commands.toList()
        )
        steps.add(newStep)
        currentAction = null
        currentProcedure = null
        currentNode = null
        commands.clear()
    }

    fun build(): GameDelta {
        return GameDelta(deltaId, steps)
    }
}
