package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

data class MovePlayerIntoSquareContext(
    val player: Player,
    val target: FieldCoordinate
) : ProcedureContext

