package com.jervisffb.engine.fsm

/**
 * Snapshot of a [MutableProcedureState]. This is unmodifiable and thread-safe, so should
 * be safe to use from the UI layer.
 *
 * It is created by calling [MutableProcedureState.createSnapshot]
 */
class ProcedureState(
    val procedure: Procedure,
    private val activeNode: Node,
    private val parentNodeState: ParentNode.State?) {
    fun getParentNodeState(): ParentNode.State {
        if (activeNode !is ParentNode) throw IllegalStateException("Current state is not a ParentNode: $activeNode")
        return parentNodeState!!
    }
    fun currentNode(): Node = activeNode
    fun name(): String = procedure.name()
    override fun toString(): String {
        return "${name()}[${activeNode.name()}]"
    }
}
