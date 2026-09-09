package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.inducements.InducementEffect

/**
 * Context class for tracking an inducement effect that has been selected for
 * use.
 *
 * See [InducementEffect]
 * See [SelectInducementEffectsContext]
 */
data class ResolveInducementEffectsContext(
    val team: Team,
    val inducement: InducementEffect
): ProcedureContext
