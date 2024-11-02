package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Context data for a player moving. This includes standing up, moving
 * a single square, jumping or leaping, but no other special actions.
 *
 * @see [MoveAction]
 */
data class MoveContext(
    val player: Player,
    val moveType: MoveType,
    val hasMoved: Boolean = false,
    val target: FieldCoordinate? = null,
    val startingSquare: FieldCoordinate = when (val location = player.location) {
        DogOut -> INVALID_GAME_STATE("Player in the dogout cannot move")
        is FieldCoordinate -> location
        is GiantLocation -> TODO("Convert startingSquare to location and adjust procedures")
    },
): ProcedureContext
