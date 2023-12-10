package dk.ilios.jervis.fsm


interface Node {
    fun name(): String = this::class.simpleName!!
}




