package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.tables.ArgueTheCallResult

data class FoulContext(
    val fouler: Player,
    val victim: Player? = null,
    val foulStandardAssists: Int = 0,
    val putTheBootInAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val injuryRoll: RiskingInjuryContext? = null,
    val hasMoved: Boolean = false,
    val hasFouled: Boolean = false,
    val spottedByTheRef: Boolean = false,
    val argueTheCall: Boolean = false,
    val argueTheCallRoll: D6Result? = null,
    val argueTheCallResult: ArgueTheCallResult? = null
) : ProcedureContext {
    val foulAssists: Int
        get() = foulStandardAssists + putTheBootInAssists
}
