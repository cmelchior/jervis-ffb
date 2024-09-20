package dk.ilios.jervis.model.context

import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.locations.GiantLocation
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Context data for a player moving. This includes standing up, moving
 * a single square, jumping or leaping, but no other special actions.
 *
 * @see [dk.ilios.jervis.procedures.actions.move.MoveAction]
 */
data class MoveContext(
    val player: Player,
    val moveType: MoveType,
    val target: FieldCoordinate? = null,
    val startingSquare: FieldCoordinate = when (val location = player.location) {
        DogOut -> INVALID_GAME_STATE("Player in the dogout cannot move")
        is FieldCoordinate -> location
        is GiantLocation -> TODO("Convert startingSquare to location and adjust procedures")
    },
): ProcedureContext
