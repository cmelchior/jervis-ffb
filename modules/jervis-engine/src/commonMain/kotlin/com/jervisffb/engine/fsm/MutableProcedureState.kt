package com.jervisffb.engine.fsm

/**
 * Class responsible for tracking the current state of a single [Procedure].
 */
class MutableProcedureState(val procedure: Procedure, initialNode: Node) {

    // Tracks the current active node
    private var activeNode: Node = initialNode

    // Track the state related to ParentNode. It should only be allowed to
    // modify this if `activeNode` is a ParentNode
    private var parentNodeState: ParentNode.State? = null

    private constructor(
        procedure: Procedure,
        history: Node,
        parentNodeState: ParentNode.State?,
    ) : this(procedure, history) {
        this.parentNodeState = parentNodeState
    }

    /**
     * If the current node is a [ParentNode], this will return its [ParentNode.State].
     * If it is any other type, an [IllegalStateException] is thrown.
     */
    fun getParentNodeState(): ParentNode.State {
        if (activeNode !is ParentNode) throw IllegalStateException("Current state is not a ParentNode: $activeNode")
        return parentNodeState!!
    }

    /**
     * Sets the state of the current active parent.
     * If the current node is not a [ParentNode], an [IllegalStateException] is thrown.
     */
    fun setParentNodeState(nextState: ParentNode.State?) {
        if (activeNode !is ParentNode) throw IllegalStateException("Current state is not a ParentNode: $activeNode")
        parentNodeState = nextState
    }

    /**
     * Returns the current active node.
     */
    fun currentNode(): Node = activeNode

    /**
     * Sets the node that is currently active.
     */
    fun setCurrentNode(node: Node) {
        activeNode = node
    }

    /**
     * Creates a read-only and thread-safe snapshot of the current state of this state object.
     */
    fun createSnapshot(): ProcedureState {
        return ProcedureState(procedure, activeNode, parentNodeState)
    }

    /**
     * Returns a name describing this state object.
     */
    fun name(): String = procedure.name()

    fun toPrettyString(): String = procedure.stateToPrettyString(activeNode)
}
