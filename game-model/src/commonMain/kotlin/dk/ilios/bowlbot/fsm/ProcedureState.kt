package dk.ilios.bowlbot.fsm

import dk.ilios.bowlbot.utils.assert

class ProcedureState(val procedure: Procedure) {
    // Track events related to ParentNode. It should only be allowed to modify this
    // if the currentNode is a ParentNode
    private val nodes: MutableList<Node> = mutableListOf()
    private val parentNodeStates: MutableList<ParentNode.State> = mutableListOf()
    constructor(procedure: Procedure, initialNode: Node): this(procedure) {
        nodes.add(initialNode)
    }
    private constructor(procedure: Procedure, history: List<Node>, parentNodeStatesHistory: List<ParentNode.State>): this(procedure) {
        nodes.addAll(history)
        parentNodeStates.addAll(parentNodeStatesHistory)
    }
    fun currentParentNodeState(): ParentNode.State  {
        return parentNodeStates.last()

    }
    fun currentNode(): Node = nodes.last()
    fun addNode(node: Node) {
        nodes.add(node)
    }
    fun copy(): ProcedureState {
        return ProcedureState(procedure, nodes.map { it }, parentNodeStates)
    }
    fun name(): String = procedure.name()

    override fun toString(): String {
        return "${name()}[${nodes.lastOrNull()?.name()}]"
    }
    fun removeLast() {
        nodes.removeLast()
    }

    fun gotoExit() {
        nodes.add(procedure.exitNode)
    }

    fun addParentNodeState(nextState: ParentNode.State) {
        parentNodeStates.add(nextState)
    }

    fun removeParentNodeState(nextState: ParentNode.State) {
        assert(parentNodeStates.isNotEmpty())
        if (parentNodeStates.last() == nextState) {
            parentNodeStates.removeLast()
        }
    }
}
