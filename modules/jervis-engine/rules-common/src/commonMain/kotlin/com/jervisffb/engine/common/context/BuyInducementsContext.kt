package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext

data class BuyInducementsContext(
    val higherCtvTeam: Team,
    val lowerCtvTeam: Team,
    val higherCtvTeamInducements: InducementsSelected? = null,
    val lowerCtvTeamInducements: InducementsSelected? = null
) : ProcedureContext {
    val ctvDifference: Int
        get() = higherCtvTeam.currentTeamValue - lowerCtvTeam.currentTeamValue
}
