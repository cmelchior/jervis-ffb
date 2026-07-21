package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.context.ProcedureContext

data class SuddenDeathContext(
    val homeRolls: List<D6Result> = emptyList(),
    val awayRolls: List<D6Result> = emptyList(),
    val rollOffs: Int = 0 // How many roll offs has happened. Roll offs with the same result are not counted.
): ProcedureContext
