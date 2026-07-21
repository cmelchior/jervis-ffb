package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate

data class StabContext(
    val attacker: Player,
    val defender: Player? = null,
    val defenderOriginalPosition: PitchCoordinate? = null,
    val stabResult: RiskingInjuryContext? = null,
): ProcedureContext
