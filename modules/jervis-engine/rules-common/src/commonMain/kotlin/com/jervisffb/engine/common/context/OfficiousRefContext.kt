package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext

data class OfficiousRefContext(
    val kickingTeamRoll: D6Result,
    val kickingTeamFanFactor: Int,
    val kickingTeamResult: Int,
    val receivingTeamRoll: D6Result? = null,
    val receivingTeamFanFactor: Int = -1,
    val receivingTeamResult: Int = -1,
    val kickingTeamPlayerSelected: Player? = null,
    val receivingTeamPlayerSelected: Player? = null,
    val kickingTeamRefereeRoll: D6Result? = null,
    val receivingTeamRefereeRoll: D6Result? = null,
): ProcedureContext
