package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.model.inducements.Timing
/**
 * Context class for tracking applying more "generic" inducement effects at the
 * given timing.
 *
 * See [InducementEffect]
 */
data class ApplyInducementEffectsContext(
    val phase: Timing,
    // If set, only inducements from one team are taken into account
    val team: Team?,
    val selectedTeam: Team? = null,
    val selectedInducement: InducementEffect? = null
): ProcedureContext
