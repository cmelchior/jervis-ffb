package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode

data class SteadyFootingRollContext(
    val player: Player,
    val mode: RiskingInjuryMode,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext {
    init {
        require(mode in listOf(RiskingInjuryMode.KNOCKED_DOWN, RiskingInjuryMode.FALLING_OVER)) {
            "Unsupported mode: $mode"
        }
    }
}

