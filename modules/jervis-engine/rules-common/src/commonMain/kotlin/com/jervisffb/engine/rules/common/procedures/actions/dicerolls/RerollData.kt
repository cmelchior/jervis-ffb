package com.jervisffb.engine.rules.common.procedures.actions.dicerolls

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.procedures.DieRoll

/**
 * Data wrapper for moving data between Roll Contexts and calculating
 * the final re-roll type (if any).
 *
 * See [D6WithRerollProcedure.AbstractChooseRerollSource]
 */
data class RerollData(
    val player: Player,
    val roll: DieRoll<*>,
    val isSuccess: Boolean?
)
