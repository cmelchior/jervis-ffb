package dk.ilios.bowlbot.fsm

interface Procedure {
    fun name(): String = this::class.simpleName!!
    val initialNode: Node
}
