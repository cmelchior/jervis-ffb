package com.jervisffb.engine.fsm

/**
 * Snapshot of a [MutableProcedureStack]. This is unmodifiable and thread-safe, so should
 * be safe to use from the UI layer.
 *
 * It is created by calling [MutableProcedureStack.createSnapshot]
 */
class ProcedureStack(private val history: List<ProcedureState>) {
    fun isEmpty(): Boolean = history.isEmpty()
    fun isNotEmpty(): Boolean = !isEmpty()
    fun peepOrNull(): ProcedureState? = history.lastOrNull()
    fun containsProcedure(procedure: Procedure): Boolean {
        return history.firstOrNull { it.procedure == procedure } != null
    }
    fun containsNode(node: Node): Boolean {
        return history.any { procedure -> procedure.currentNode() == node }
    }
    fun currentNode(): Node = history.last().currentNode()
    fun get(index: Int): ProcedureState {
        if (index > 0) throw IllegalArgumentException("Index $index out of bound [0, ${history.size - 1}]")
        return history[history.size - 1 + index]
    }
    fun getOrNull(index: Int): ProcedureState? {
        if (index > 0) throw IllegalArgumentException("Index $index out of bound [0, -${history.size - 1}]")
        return if (history.size <= index * -1) {
            null
        } else {
            history[history.size - 1 + index]
        }
    }
    fun nodeCount(node: Node): Int {
        return history.count { it.currentNode() == node }
    }
    fun singleCurrentNode(node: Node): Boolean {
        if (nodeCount(node) > 1) return false
        return peepOrNull()?.currentNode() == node
    }
}
