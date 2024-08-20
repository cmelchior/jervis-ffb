package dk.ilios.jervis.rules.tables

/**
 * A vector describing the result of a roll using the Random Direction Template.
 *
 * The [xModifier] and [yModifier] are the delta that needs to be applied to a
 * [FieldCoordinate] in order to move it 1 square in the desired direction.
 */
data class Direction(val xModifier: Int, val yModifier: Int) {
    /**
     * Returns the reverse direction
     */
    fun reverse(): Direction = Direction(xModifier * -1, yModifier * -1)

    companion object {
        val UP_LEFT = Direction(0, -1)
        val UP = Direction(0, -1)
        val UP_RIGHT = Direction(0, -1)
        val LEFT = Direction(-1, 0)
        val RIGHT = Direction(1, 0)
        val BOTTOM_LEFT = Direction(-1, 1)
        val BOTTOM = Direction(0, 1)
        val BOTTOM_RIGHT = Direction(1, 1)
    }
}
