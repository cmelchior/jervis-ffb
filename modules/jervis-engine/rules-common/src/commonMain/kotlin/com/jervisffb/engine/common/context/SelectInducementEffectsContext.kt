package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.model.inducements.Timing

/**
 * Context class used during selection of which inducement effects to use for
 * a given timing.
 *
 * See [InducementEffect]
 * See [ResolveInducementEffectsContext]
 */
data class SelectInducementEffectsContext(
    // Which timing is being considered
    val phase: Timing,
    // If set, only inducements from one team are taken into account
    val team: Team?,
): ProcedureContext
