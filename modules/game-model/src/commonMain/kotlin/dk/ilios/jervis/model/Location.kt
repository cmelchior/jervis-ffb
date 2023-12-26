package dk.ilios.jervis.model

/**
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */

sealed interface Location
// (0, 0) is (top, left)
data class FieldCoordinate(val x: Int, val y: Int): Location
data object DogOut: Location


