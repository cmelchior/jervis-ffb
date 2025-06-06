package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

data class PassingInterferenceContext(
    val thrower: Player,
    // Target coordinates of the throw after resolving the throw type, but not including
    // scatter from a failed interception.
    val target: FieldCoordinate,
    val interferencePlayer: Player? = null, // Player doing the interference, if any.
    val interferenceRoll: PassingInterferenceRollContext? = null,
    val didDeflect: Boolean = false,
    val didIntercept: Boolean = false,
): ProcedureContext {
    // After passing interference, is the pass step allowed to continue or must it end
    val continueThrow = !didDeflect
}
