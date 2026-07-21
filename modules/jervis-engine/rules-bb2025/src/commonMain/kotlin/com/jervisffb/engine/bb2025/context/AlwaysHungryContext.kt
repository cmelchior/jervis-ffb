package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class AlwaysHungryContext(
    val thrower: Player,
    val thrownPlayer: Player,
    val isHungryRoll: D6DieRoll? = null,
    val isHungry: Boolean = false,
    val squirmFreeRoll: D6DieRoll? = null,
    val squirmedFree: Boolean = false,
) : ProcedureContext
