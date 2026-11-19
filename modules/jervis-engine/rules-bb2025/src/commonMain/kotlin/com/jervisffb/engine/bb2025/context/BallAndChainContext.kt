package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.D3DieRoll

data class BallAndChainContext(
    val player: Player,
    val orientation: Direction,
    val origin: PitchCoordinate = player.coordinates,
    val directionRoll: D3DieRoll? = null,
    val destination: PitchCoordinate? = null,
    val rushFailed: Boolean = false,
    val occupant: Player? = null,
) : ProcedureContext
