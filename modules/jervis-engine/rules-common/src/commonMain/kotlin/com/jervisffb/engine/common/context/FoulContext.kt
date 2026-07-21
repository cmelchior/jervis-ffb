package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class FoulContext(
    val fouler: Player,
    val victim: Player? = null,
    val offensiveAssists: Int = 0,
    val putTheBootInAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val injuryRoll: RiskingInjuryContext? = null,
    val hasMoved: Boolean = false,
    val hasFouled: Boolean = false,
    val spottedByTheRef: Boolean = false,
    val argueTheCall: BeingSentOffContext? = null
) : ProcedureContext
