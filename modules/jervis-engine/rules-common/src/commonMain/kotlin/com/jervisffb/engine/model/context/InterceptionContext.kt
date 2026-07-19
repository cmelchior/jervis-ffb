package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate

data class InterceptionContext(
    val thrower: Player,
    // Target coordinates of the throw after resolving the throw type, but not including
    // scatter from a failed interception.
    val target: PitchCoordinate,
    val interceptingPlayer: Player? = null, // Player doing the interception, if any.
    val useCloudBurster: Boolean = false,
    val useExtraArms: Boolean = false, // If intercepting player is using Extra Arms or not
    val interceptionRoll: InterceptionRollContext? = null,
    val didIntercept: Boolean = false,
) : ProcedureContext {
    // After passing interference, is the pass step allowed to continue or must it end
    val continueThrow = !didIntercept
}
