package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command

/**
 * This class represents a dynamic Finite-State-Machine (FSM) that makes it possible to expand nodes dynamically,
 * i.e., you do not need to define the entire FSM from the start, but it can be done as you navigate the state
 * machine.
 *
 * Conceptually this is modeled as a stack of [Procedure]s, where you can add extra layers
 * through the use of [ParentNode]s that will put a new [Procedure] on the stack. Each layer remembers their
 * current node, so when you pop a [Procedure], the stack remembers where to continue from in the parent layer.
 *
 * --------------
 *
 * Note, both [ParentNode] and [Procedure] has a lifecycle attached. See these classes for mor details.
 *
 * Navigating the FSM is done through the use of the [ActionDescriptor], [GameAction] and [Command] classes.
 *
 * - [ActionDescriptor]: Describes the valid actions that a given [Node] will accept
 * - [GameAction]: Are the actual action the [Node] is asked to handle
 * - [Command]: Wraps the intent of modifying the FSM or some other state.
 *
 * Currently, three node types are supported:
 * - [ActionNode]: A node type that requires user input.
 * - [ParentNode]: A node type that will load a new [Procedure] and put it on the stack.
 * - [ComputationNode]: A subtype of [ActionNode] that only accept a `Continue` action
 *
 * But new node types can be introduced by subclassing the [Node] interface.
 */
class ProcedureStack {

    // Store the list of procedures currently on the stack.
    // The `history` is used as a Stack (First-In-Last-Out)
    // Using an ArrayList vs. ArrayDequeue due to a better resize policy
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


    // TODO Why is this method here?
    fun addNode(nextNode: Node) = history.last().setCurrentNode(nextNode)

    // TODO Why is this method here?
    // fun removeNode() = history.last().removeLast()

    /**
     * Returns `true` if the given [Procedure] is part of the current stack.
     */
    fun contains(procedure: Procedure): Boolean {
        return history.firstOrNull { it.procedure == procedure } != null
    }

    /**
     * Returns the current active [Node].
     * Will throw [NoSuchElementException] is [isEmpty] returns `true`.
     */
    fun currentNode(): Node = history.last().currentNode()
}
