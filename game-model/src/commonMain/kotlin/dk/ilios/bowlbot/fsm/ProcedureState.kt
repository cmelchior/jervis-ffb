package dk.ilios.bowlbot.fsm

class ProcedureState(val procedure: Procedure) {
    private val nodes: MutableList<Node> = mutableListOf()
    constructor(procedure: Procedure, initialNode: Node): this(procedure) {
        nodes.add(initialNode)
    }
    private constructor(procedure: Procedure, history: List<Node>): this(procedure) {
        nodes.addAll(history)
    }
    fun currentNode(): Node = nodes.last()
    fun addNode(node: Node) {
        nodes.add(node)
    }
    fun copy(): ProcedureState {
        return ProcedureState(procedure, nodes.map { it })
    }
    fun name(): String = procedure.name()

    override fun toString(): String {
        return "${name()}[${nodes.lastOrNull()?.name()}]"
    }
    fun removeLast() {
        nodes.removeLast()
    }
}
