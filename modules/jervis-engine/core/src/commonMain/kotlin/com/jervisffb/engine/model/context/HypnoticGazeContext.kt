package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class HypnoticGazeContext(
    val gazer: Player,
    val hasMoved: Boolean = false,
    val hasGazed: Boolean = false,
    val target: Player? = null,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
) : ProcedureContext
