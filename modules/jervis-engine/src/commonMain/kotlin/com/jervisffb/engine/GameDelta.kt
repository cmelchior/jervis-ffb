package com.jervisffb.engine

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.CompositeCommand
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.TeamId
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
    val id: GameActionId,
    val steps: List<ActionStep>,
    val owner: TeamId? = null,
    // Game Delta is a reversed delta. Used when undoing deltas.
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
     * Return a copy of this delta, but with all actions, commands reversed. This is used
     * when Undoing actions.
     */
    fun reverse(): GameDelta {
        return GameDelta(
            id = id,
            steps = steps.reversed().map {
                it.copy(commands = it.commands.reversed())
            },
            owner = owner,
            reversed = true
        )
    }
}

internal class DeltaBuilder(val actionId: GameActionId, val actionOwner: TeamId? = null) {

    private val steps = mutableListOf<ActionStep>()

    private var currentAction: GameAction? = null
    private var currentProcedure: Procedure? = null
    private var currentNode: Node? = null
    private val commands: MutableList<Command> = mutableListOf()
    private val logs: MutableList<LogEntry> = mutableListOf()

    // For now treat everything between public actions as one step, even if it might involve multiple node
    // transitions
    fun beginAction(
        action: GameAction,
        procedure: Procedure,
        node: Node
    ) {
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
            currentAction!!,
            currentProcedure!!,
            currentNode!!,
            commands.toList()
        )
        steps.add(newStep)
        currentAction = null
        currentProcedure = null
        currentNode = null
        commands.clear()
    }

    fun build(): GameDelta {
        return GameDelta(actionId, steps, actionOwner)
    }
}
