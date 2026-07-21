package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class ProjectileVomitContext(
    val attacker: Player,
    val attackerOriginalCoordinates: PitchCoordinate,
    val defender: Player? = null,
    val defenderOriginalCoordinates: PitchCoordinate? = null,
    val vomitRoll: D6DieRoll? = null,
    val injuryResult: RiskingInjuryContext? = null,
    val isSuccess: Boolean = false,
): ProcedureContext
