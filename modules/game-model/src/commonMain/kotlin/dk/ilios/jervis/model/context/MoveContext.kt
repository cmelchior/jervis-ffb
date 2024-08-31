package dk.ilios.jervis.model.context

import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player

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
    val startingSquare: FieldCoordinate = player.location.coordinate,
): ProcedureContext
