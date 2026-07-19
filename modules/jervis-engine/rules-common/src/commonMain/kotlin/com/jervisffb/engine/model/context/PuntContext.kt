package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.D3DieRoll
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class PuntContext(
    val punter: Player,
    val hasMoved: Boolean = false,
    val hasPunted: Boolean = false,
    val selectedDirection: Direction? = null,
    val directionRoll: D3DieRoll? = null,
    val kickDirection: Direction? = null,
    val distanceRoll: D6DieRoll? = null,
) : ProcedureContext
