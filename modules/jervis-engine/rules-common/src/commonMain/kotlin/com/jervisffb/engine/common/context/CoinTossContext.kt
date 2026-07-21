package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext

data class CoinTossContext(
    val sideSelected: Coin,
    val coinToss: CoinTossResult? = null,
    val winner: Team? = null,
): ProcedureContext
