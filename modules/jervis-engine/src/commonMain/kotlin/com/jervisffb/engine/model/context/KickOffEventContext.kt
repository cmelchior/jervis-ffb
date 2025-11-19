package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.rules.common.tables.TableResult

data class KickOffEventContext(
    val roll: DiceRollResults,
    val result: TableResult,
    val scatterBallBeforeLanding: Boolean = false // If Changing Weather rolled Perfect Conditions
): ProcedureContext

