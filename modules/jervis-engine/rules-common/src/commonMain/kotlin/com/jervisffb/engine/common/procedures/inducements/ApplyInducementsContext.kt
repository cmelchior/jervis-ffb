package com.jervisffb.engine.common.procedures.inducements

import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext

/**
 * Context describing which inducements have been chosen and are ready to be
 * applied to a team. Consumed by the ruleset's `applyInducementsStep`.
 */
data class ApplyInducementsContext(
    val team: Team,
    val inducements: InducementsSelected,
    val rollForPrayers: Int = 0,
) : ProcedureContext
