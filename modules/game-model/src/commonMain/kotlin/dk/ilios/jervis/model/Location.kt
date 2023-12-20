package dk.ilios.jervis.model

/**
 * Top-left is (0,0), bottom-left is (25, 14) for a normal Blood Bowl Field.
 */

sealed interface Location
data class FieldCoordinate(val x: UInt, val y: UInt): Location
data object DogOut: Location


