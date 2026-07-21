package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.D3DieRoll
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class SwoopContext(
    val player: Player,
    val selectedDirection: Direction? = null,
    val directionRoll: D3DieRoll? = null,
    val rolledDirection: Direction? = null,
    val distanceRoll: D6DieRoll? = null,
    val landsAt: PitchCoordinate? = null,
    // If the player lands outside the pitch, this is the location they left the field.
    val outOfBoundsAt: PitchCoordinate? = null,
): ProcedureContext {
    val coordinate: PitchCoordinate = player.coordinates
}
