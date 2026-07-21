package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.bb2025.procedures.actions.block.BreatheFireResult
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class BreatheFireContext(
    val attacker: Player,
    val defender: Player? = null,
    val breatheRoll: D6DieRoll? = null,
    val result: BreatheFireResult? = null,
    val injuryResult: RiskingInjuryContext? = null,
): ProcedureContext
