package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class ChainsawContext(
    val attacker: Player,
    val attackerOriginalCoordinates: PitchCoordinate,
    val defender: Player? = null,
    val defenderOriginalCoordinates: PitchCoordinate? = null,
    val kickbackRoll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext
