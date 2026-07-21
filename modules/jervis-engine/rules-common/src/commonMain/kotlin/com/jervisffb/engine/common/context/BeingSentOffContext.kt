package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.ArgueTheCallResult

data class BeingSentOffContext(
    val player: Player,
    val argueTheCall: Boolean = false,
    val argueTheCallRoll: D6DieRoll? = null,
    val argueTheCallResult: ArgueTheCallResult? = null,
    val isBribeAvailable: Boolean = false,
    val usedBribe: Boolean = false,
    val bribeRoll: D6DieRoll? = null,
): ProcedureContext
