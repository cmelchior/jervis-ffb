package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command

/**
 * This class is the main way to organize the rules of Blood Bowl, or more correctly the "flow" of the game.
 *
 * The rules as implemented using a dynamic Finite-State-Machine (FSM). This makes it possible to expand nodes
 * dynamically, i.e., you do not need to define the entire FSM from the start, but it can be done as you navigate
 * the state machine.
 *
 * Conceptually this is modeled as a stack of [Procedure]s. Each procedure is its own FSM where "state"
 * is encapsulated in a [Node]. New procedures are pushed to the stack through the use of [ParentNode]s. Each
 * procedure remembers their current node, so when you pop a [Procedure], the stack can automatically resume in the
 * parent procedure.
 *
 * Navigating the FSM is done through the use of the [GameActionDescriptor], [GameAction] and [Command] classes.
 *
 * - [GameActionDescriptor]: Describes the valid actions that a given [Node] will accept.
 * - [GameAction]: Are the actual action the [Node] is asked to handle.
 * - [Command]: Wraps the modification of the FSM or some other state.
 *
 * Currently, three node types are supported:
 *
 * - [ActionNode]: A node type that requires user input.
 * - [ParentNode]: A node type that will load a new [Procedure] and put it on the stack.
 * - [ComputationNode]: A subtype of [ActionNode] that only accept a [Continue] action.
 *
 * New node types can be introduced by subclassing the [Node] interface.
 */
class ProcedureStack {

    // Store the list of procedures currently on the stack.
    // The `history` is used as a Stack (First-In-Last-Out)
    // It is using an ArrayList vs. ArrayDequeue due to a better resize policy
    private val history: MutableList<ProcedureState> = mutableListOf()

    /**
     * Returns `true` if the stack is empty, i.e., no events can be processed.
     * This state should only be allowed when either starting the FSM or it is ready
     * to close down because there is no more work to do.
     */
    fun isEmpty(): Boolean = history.isEmpty()

    /**
     * Push an existing [ProcedureStack] to the stack. This makes it possible to push a
     * pre-configured procedure, i.e., one that doesn't start at [Procedure.initialNode].
     */
    fun pushProcedure(procedure: ProcedureState) {
        history.add(procedure)
    }

    /**
     * Push a new [Procedure] to the stack. This will create a new [ProcedureState].
     */
    fun pushProcedure(procedure: Procedure) {
        history.add(ProcedureState(procedure, procedure.enterNode))
    }

    /**
     * Removes the current [Procedure] from the stack and returns it.
     * Will throw [NoSuchElementException] if the stack is empty.
     */
    fun popProcedure(): ProcedureState = history.removeLast()

    /**
     * Returns the current active [ProcedureState] (if any)
     */
    fun peepOrNull(): ProcedureState? = history.lastOrNull()

    /**
     * Returns `true` if the given [Procedure] is part of the current stack.
     */
    fun containsProcedure(procedure: Procedure): Boolean {
        return history.firstOrNull { it.procedure == procedure } != null
    }

    /**
     * Returns `true` if the given [Node] is the active node at any part
     * of the stack.
     */
    fun containsNode(node: Node): Boolean {
        // TODO This can run into ConcurrentModification
        //  at com.jervisffb.ui.game.viewmodel.SidebarViewModel$special$$inlined$map$1$2
        return history.any { procedure -> procedure.currentNode() == node }
    }

    /**
     * Returns the current active [Node].
     * Will throw [NoSuchElementException] if [isEmpty] returns `true`.
     */
    fun currentNode(): Node = history.last().currentNode()

    /**
     * Returns the procedure at the given index. Indexes are i <= 0, so 0 is the
     * current procedure, -1 is the parent, -2 is the parents parent, and so on.
     */
    fun get(index: Int): ProcedureState {
        if (index > 0) throw IllegalArgumentException("Index $index out of bound [0, ${history.size - 1}]")
        return history[history.size - 1 + index]
    }

    /**
     * Returns the procedure at the given index. Indexes are i <= 0, so 0 is the
     * current procedure, -1 is the parent, -2 is the parents parent, and so on.
     *
     * If the stack is too shallow to contain the index, `null` is returned.
     */
    fun getOrNull(index: Int): ProcedureState? {
        if (index > 0) throw IllegalArgumentException("Index $index out of bound [0, -${history.size - 1}]")
        return if (history.size <= index * -1) {
            null
        } else {
            history[history.size - 1 + index]
        }
    }

    /**
     * Returns the number of active nodes in the entire stack.
     *
     * This can, e.g., be used to detect if a chain of bounces or catches are
     * happening.
     */
    fun nodeCount(node: Node): Int {
        return history.count { it.currentNode() == node }
    }

    /**
     * Returns `true` if the current active node is the provided one, and it doesn't
     * exist anywhere else in the stack.
     */
    fun singleCurrentNode(node: Node): Boolean {
        if (nodeCount(node) > 1) return false
        return peepOrNull()?.currentNode() == node
    }
}
