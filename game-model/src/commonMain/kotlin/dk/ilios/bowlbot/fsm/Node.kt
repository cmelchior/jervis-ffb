package dk.ilios.bowlbot.fsm


interface Node {
    fun name(): String = this::class.simpleName!!
}




