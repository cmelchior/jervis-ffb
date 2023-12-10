 package dk.ilios.jervis.actions

class SpaceSelected(val x: UInt, val y: UInt): Action {
    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}
