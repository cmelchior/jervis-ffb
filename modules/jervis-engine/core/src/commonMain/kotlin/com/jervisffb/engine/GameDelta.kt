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
import kotlin.reflect.KClass

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
    // Game Delta is being reversed. Used during undoing deltas.
    // When this is true, steps are reversed. This means the last entry is always
    // the last action that was executed.
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

data class NodeStep(val procedure: Procedure, val clazz: KClass<out Node>) {
    override fun toString(): String {
        return "${procedure.name()}[${clazz.simpleName}]"
    }
}

internal class DeltaBuilder(val actionId: GameActionId, val actionOwner: TeamId? = null) {

    private val steps = mutableListOf<ActionStep>()

    private var currentAction: GameAction? = null
    private var startingProcedure: Procedure? = null
    private var startingNode: Node? = null
    private var intermediateNodes = mutableListOf<NodeStep>()
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
        startingProcedure = procedure
        startingNode = node
        intermediateNodes.clear()
        commands.clear()
    }

    // As the GameAction is processed, we might transfer control across multiple nodes.
    // This method should be called every time we enter a new node as it makes it possible
    // for listeners to follow the flow. This is useful for debugging or for certain UI flows
    // like Fumblerooski.
    fun addIntermediateNode(procedure: Procedure?, node: Node?) {
        if (procedure != null && node != null) {
            intermediateNodes.add(NodeStep(procedure, node::class))
        }
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
            startingProcedure!!,
            startingNode!!,
            intermediateNodes.toList(), // Copy list
            commands.toList() // Copy list
        )
        steps.add(newStep)
        currentAction = null
        startingProcedure = null
        startingNode = null
        intermediateNodes.clear()
        commands.clear()
    }

    fun build(): GameDelta {
        return GameDelta(actionId, steps, actionOwner)
    }
}
