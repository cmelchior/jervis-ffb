package dk.ilios.jervis.rules.tables

/**
 * A vector describing the result of a roll using the Random Direction Template.
 *
 * The [xModifier] and [yModifier] are the delta that needs to be applied to a
 * [FieldCoordinate] in order to move it 1 square in the desired direction.
 */
data class Direction(val xModifier: Int, val yModifier: Int)

