package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class HandOffContext(
    val thrower: Player,
    val catcher: Player? = null,
    val hasMoved: Boolean = false,
    val hasHandedOff: Boolean = false
) : ProcedureContext {
}
