package dk.ilios.jervis.fsm

class ProcedureStack {
    private val history: ArrayDeque<ProcedureState> = ArrayDeque()
    fun isEmpty(): Boolean = history.isEmpty()
    fun currentNode(): Node = history.first().currentNode()
    fun pushProcedure(procedure: ProcedureState) {
        history.addFirst(procedure)
    }
    fun pushProcedure(procedure: Procedure) {
        history.addFirst(ProcedureState(procedure, procedure.enterNode))
    }
    fun popProcedure(): ProcedureState = history.removeFirst()
    fun firstOrNull(): ProcedureState? = history.firstOrNull()
    fun addNode(nextNode: Node) = history.first().addNode(nextNode)
    fun removeNode() = history.first().removeLast()

    /**
     * Check if a certain procedure is part of the current stack
     */
    fun contains(procedure: Procedure): Boolean {
        return history.firstOrNull { it.procedure == procedure } != null
    }
}
